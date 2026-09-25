package com.exemple.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.exemple.backend.entity.Exercice;
import com.exemple.backend.entity.Presence;
import com.exemple.backend.entity.Relecture;
import com.exemple.backend.entity.Session;
import com.exemple.backend.entity.SourcePresence;
import com.exemple.backend.entity.StatutExercice;
import com.exemple.backend.repository.ExerciceRepository;
import com.exemple.backend.repository.PresenceRepository;
import com.exemple.backend.repository.RelectureRepository;
import com.exemple.backend.repository.SessionRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Issue #7 — EF7 : le systeme assigne un relecteur au hasard parmi les
 * presents, jamais l'auteur (RG4), un seul relecteur par exercice (RG5).
 * RG13 : sans eligible, l'exercice reste EN_ATTENTE_ASSIGNATION et
 * l'assignation est retentee a chaque nouvelle presence.
 */
@SpringBootTest
@Transactional
class AssignationRelecteurTest {

	@Autowired
	private RelectureService relectureService;

	@Autowired
	private SessionRepository sessions;

	@Autowired
	private PresenceRepository presences;

	@Autowired
	private ExerciceRepository exercices;

	@Autowired
	private RelectureRepository relectures;

	private Presence presenter(long sessionId, long etudiantId) {
		Session session = sessions.findById(sessionId).orElseThrow();
		return presences.save(new Presence(session, etudiantId, OffsetDateTime.now(), SourcePresence.ETUDIANT));
	}

	private Exercice exerciceDepose(long sessionId, long auteurId) {
		Session session = sessions.findById(sessionId).orElseThrow();
		return exercices.save(new Exercice(session, auteurId, "https://exemples.dev/exo", OffsetDateTime.now()));
	}

	@Test
	void assignation_neDesignJamaisLAuteur_RG4() {
		exerciceDepose(1L, 1L);
		// Seul l'auteur est present : aucun eligible (RG13).
		presenter(1L, 1L);
		relectureService.tenterAssignations(1L);

		Exercice exercice = exercices.findBySessionIdAndAuteurId(1L, 1L).orElseThrow();
		assertThat(exercice.getStatut()).isEqualTo(StatutExercice.EN_ATTENTE_ASSIGNATION);
		assertThat(relectures.findByExerciceId(exercice.getId())).isEmpty();
	}

	@Test
	void assignation_prendUnRelecteurParmiLesPresentsEtPasseEnAttenteRelecture() {
		Exercice exercice = exerciceDepose(1L, 1L);
		presenter(1L, 1L);
		presenter(1L, 2L);
		presenter(1L, 3L);

		relectureService.tenterAssignations(1L);

		Exercice misAJour = exercices.findById(exercice.getId()).orElseThrow();
		assertThat(misAJour.getStatut()).isEqualTo(StatutExercice.EN_ATTENTE_RELECTURE);

		Optional<Relecture> relecture = relectures.findByExerciceId(exercice.getId());
		assertThat(relecture).isPresent();
		// RG4 : le relecteur designe n'est jamais l'auteur.
		assertThat(relecture.get().getRelecteurId()).isNotEqualTo(1L);
		// RG5 : il fait partie des presents de la session.
		assertThat(relecture.get().getRelecteurId()).isIn(2L, 3L);
	}

	@Test
	void nouvellePresence_retenteAssignation_RG13() {
		Exercice exercice = exerciceDepose(1L, 1L);
		presenter(1L, 1L);
		relectureService.tenterAssignations(1L);
		assertThat(exercices.findById(exercice.getId()).orElseThrow().getStatut())
				.isEqualTo(StatutExercice.EN_ATTENTE_ASSIGNATION);

		// Un deuxieme etudiant se presente : l'assignation devient possible.
		presenter(1L, 5L);
		relectureService.tenterAssignations(1L);

		Exercice misAJour = exercices.findById(exercice.getId()).orElseThrow();
		assertThat(misAJour.getStatut()).isEqualTo(StatutExercice.EN_ATTENTE_RELECTURE);
		assertThat(relectures.findByExerciceId(exercice.getId()).orElseThrow().getRelecteurId())
				.isEqualTo(5L);
	}

	@Test
	void chaqueExerciceNAuPlusUnRelecteur_RG5() {
		Exercice e1 = exerciceDepose(1L, 1L);
		Exercice e2 = exerciceDepose(1L, 2L);
		presenter(1L, 1L);
		presenter(1L, 2L);
		presenter(1L, 3L);

		relectureService.tenterAssignations(1L);

		for (Exercice exercice : List.of(e1, e2)) {
			assertThat(relectures.findByExerciceId(exercice.getId())).isPresent();
		}
		// Un seul relecteur par exercice : pas de relecture en double.
		assertThat(relectures.findByExerciceSessionId(1L)).hasSize(2);
	}
}
