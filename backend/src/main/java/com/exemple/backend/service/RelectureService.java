package com.exemple.backend.service;

import com.exemple.backend.entity.CodeErreur;
import com.exemple.backend.entity.Exercice;
import com.exemple.backend.entity.Presence;
import com.exemple.backend.entity.Relecture;
import com.exemple.backend.entity.Session;
import com.exemple.backend.entity.StatutExercice;
import com.exemple.backend.repository.ExerciceRepository;
import com.exemple.backend.repository.PresenceRepository;
import com.exemple.backend.repository.RelectureRepository;
import com.exemple.backend.repository.SessionRepository;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regles de gestion des exercices et relectures :
 * EF5-EF10, RG4 (jamais son propre exercice), RG5 (un relecteur, parmi les
 * presents), RG6 (anonymat), RG7 (note 0-20), RG8 (definitive), RG9 (statut
 * en attente), RG10 (cloture), RG11 (remplacement du lien), RG13 (pas de
 * relecteur eligible => EN_ATTENTE_ASSIGNATION, retente a chaque presence).
 */
@Service
public class RelectureService {

	private final SessionRepository sessions;
	private final PresenceRepository presences;
	private final ExerciceRepository exercices;
	private final RelectureRepository relectures;

	public RelectureService(SessionRepository sessions, PresenceRepository presences,
			ExerciceRepository exercices, RelectureRepository relectures) {
		this.sessions = sessions;
		this.presences = presences;
		this.exercices = exercices;
		this.relectures = relectures;
	}

	/** EF5 : depot du lien d'exercice (statut initial EF5/RG9). */
	@Transactional
	public Exercice deposer(Long sessionId, Long etudiantId, String lien) {
		Session session = sessionOuverte(sessionId);
		if (exercices.findBySessionIdAndAuteurId(sessionId, etudiantId).isPresent()) {
			throw new RegleMetierException(CodeErreur.DEJA_PRESENT, "Un exercice est deja depose par cet etudiant sur cette session");
		}
		return exercices.save(new Exercice(session, etudiantId, lien, OffsetDateTime.now()));
	}

	/** EF6/RG11 : remplacement du lien tant qu'aucune relecture n'a commence. */
	@Transactional
	public Exercice remplacerLien(Long exerciceId, Long etudiantId, String nouveauLien) {
		Exercice exercice = exerciceExistant(exerciceId);
		sessionOuverte(exercice.getSession().getId());
		if (!exercice.getAuteurId().equals(etudiantId)) {
			throw new RegleMetierException(CodeErreur.AUTEUR_NON_ELIGIBLE, "Seul l'auteur peut remplacer le lien");
		}
		if (exercice.getStatut() == StatutExercice.RELUE) {
			throw new RegleMetierException(CodeErreur.RELECTURE_DEJA_COMMENCEE, "Une relecture a deja commence, lien verrouille");
		}
		exercice.remplacerLien(nouveauLien);
		return exercice;
	}

	/**
	 * RG5/RG13 : designe au hasard un relecteur parmi les presents de la session,
	 * jamais l'auteur (RG4). Appele a chaque nouvelle presence marquee.
	 * Sans eligible, l'exercice reste EN_ATTENTE_ASSIGNATION.
	 */
	@Transactional
	public void tenterAssignations(Long sessionId) {
		List<Long> presentsIds = presences.findBySessionId(sessionId).stream()
				.map(Presence::getEtudiantId)
				.toList();
		for (Exercice exercice : exercices.findBySessionIdAndStatut(sessionId, StatutExercice.EN_ATTENTE_ASSIGNATION)) {
			List<Long> eligibles = new ArrayList<>(presentsIds);
			eligibles.remove(exercice.getAuteurId());
			if (eligibles.isEmpty()) {
				continue; // RG13 : pas de relecteur eligible pour l'instant
			}
			Long relecteurId = eligibles.get(ThreadLocalRandom.current().nextInt(eligibles.size()));
			relectures.save(new Relecture(exercice, relecteurId, 0, "", OffsetDateTime.now()));
			exercice.assignerRelecteur();
		}
	}

	/** EF8 : le relecteur rend note et commentaire. Definitif (RG8). */
	@Transactional
	public Relecture rendre(Long exerciceId, Long relecteurId, int note, String commentaire) {
		Relecture relecture = relectures.findByExerciceId(exerciceId)
				.orElseThrow(() -> new RegleMetierException(CodeErreur.INTROUVABLE, "Aucune relecture attendue sur cet exercice"));
		Exercice exercice = relecture.getExercice();
		sessionOuverte(exercice.getSession().getId());
		if (relecteurId.equals(exercice.getAuteurId())) {
			throw new RegleMetierException(CodeErreur.AUTEUR_NON_ELIGIBLE, "Un etudiant ne peut pas relire son propre exercice");
		}
		if (!relecteurId.equals(relecture.getRelecteurId())) {
			throw new RegleMetierException(CodeErreur.INTROUVABLE, "Cet exercice n'est pas assigne a ce relecteur");
		}
		if (exercice.getStatut() == StatutExercice.RELUE) {
			throw new RegleMetierException(CodeErreur.RELECTURE_DEJA_RENDUE, "Relecture deja rendue, elle est definitive");
		}
		// RG8 : la relecture d'assignation est mutee en relecture rendue
		// (l'index unique uq_relecture_exercice interdit une seconde ligne).
		relecture.rendre(note, commentaire);
		Relecture enregistree = relectures.save(relecture);
		exercice.marquerRelue();
		return enregistree;
	}

	/** EF9/RG6 : l'auteur consulte sa note, sans jamais voir le relecteur. */
	@Transactional(readOnly = true)
	public Relecture resultat(Long exerciceId, Long auteurId) {
		Exercice exercice = exerciceExistant(exerciceId);
		if (!exercice.getAuteurId().equals(auteurId)) {
			throw new RegleMetierException(CodeErreur.AUTEUR_NON_ELIGIBLE, "Resultat reserve a l'auteur");
		}
		return relectures.findByExerciceId(exerciceId).orElse(null);
	}

	/** Relecteur designe d'un exercice, pour le tableau du formateur uniquement (RG6). */
	@Transactional(readOnly = true)
	public Long relecteurDe(Long exerciceId) {
		return relectures.findByExerciceId(exerciceId).map(Relecture::getRelecteurId).orElse(null);
	}

	/** EF10 : tableau de bord formateur, par etudiant. */
	@Transactional(readOnly = true)
	public List<LigneTableau> tableau(Long sessionId) {
		List<Presence> presents = presences.findBySessionId(sessionId);
		List<Exercice> deposees = exercices.findBySessionId(sessionId);
		Map<Long, Exercice> parAuteur = deposees.stream()
				.collect(Collectors.toMap(Exercice::getAuteurId, Function.identity(), (a, b) -> a));

		List<Long> idsEtudiants = presents.stream().map(Presence::getEtudiantId).collect(Collectors.toList());
		deposees.stream().map(Exercice::getAuteurId).filter(id -> !idsEtudiants.contains(id)).forEach(idsEtudiants::add);

		return idsEtudiants.stream().map(id -> ligne(sessionId, id, presents, parAuteur.get(id)))
				.sorted(Comparator.comparing(LigneTableau::etudiantId))
				.toList();
	}

	private LigneTableau ligne(Long sessionId, Long etudiantId, List<Presence> presents, Exercice exercice) {
		long nbPresences = presents.stream().filter(p -> p.getEtudiantId().equals(etudiantId)).count();
		long enAttente = relectures.findByExerciceSessionId(sessionId).stream()
				.filter(r -> exercice == null || !r.getExercice().getId().equals(exercice.getId()))
				.count();
		Double moyenne = null;
		if (exercice != null && exercice.getStatut() == StatutExercice.RELUE) {
			moyenne = relectures.findByExerciceId(exercice.getId()).map(Relecture::getNote).stream()
					.mapToInt(Integer::intValue).average().orElse(0d);
		}
		return new LigneTableau(etudiantId, null, nbPresences,
				exercice == null ? List.of() : List.of(exercice), moyenne, enAttente);
	}

	private Session sessionOuverte(Long sessionId) {
		Session session = sessions.findById(sessionId)
				.orElseThrow(() -> new RegleMetierException(CodeErreur.INTROUVABLE, "Session inconnue"));
		if (session.isCloturee()) {
			throw new RegleMetierException(CodeErreur.SESSION_CLOTUREE, "Session cloturee : operation refusee");
		}
		return session;
	}

	private Exercice exerciceExistant(Long exerciceId) {
		return exercices.findById(exerciceId)
				.orElseThrow(() -> new RegleMetierException(CodeErreur.INTROUVABLE, "Exercice inconnu"));
	}

	public record LigneTableau(Long etudiantId, String nom, long presences, List<Exercice> exercices, Double moyenne, long relecturesEnAttente) {
	}
}
