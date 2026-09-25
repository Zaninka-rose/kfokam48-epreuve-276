package com.exemple.backend.service;

import com.exemple.backend.domain.CodeErreur;
import com.exemple.backend.domain.Presence;
import com.exemple.backend.domain.Session;
import com.exemple.backend.domain.SourcePresence;
import com.exemple.backend.domain.TentativeCode;
import com.exemple.backend.repository.PresenceRepository;
import com.exemple.backend.repository.SessionRepository;
import com.exemple.backend.repository.TentativeCodeRepository;
import com.exemple.backend.service.RegleMetierException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.Duration;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regles de gestion des sessions et presences :
 * RG1/RG2 (expiration 15 min), RG3 (blocage 5 erreurs), EF4 (double presence),
 * EF11 (presence formateur), EF12 (cloture).
 */
@Service
public class SessionService {

	public static final Duration DUREE_CODE = Duration.ofMinutes(15);

	private static final String ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
	private static final SecureRandom ALEATOIRE = new SecureRandom();

	private final SessionRepository sessions;
	private final PresenceRepository presences;
	private final TentativeCodeRepository tentatives;

	public SessionService(SessionRepository sessions, PresenceRepository presences,
			TentativeCodeRepository tentatives) {
		this.sessions = sessions;
		this.presences = presences;
		this.tentatives = tentatives;
	}

	/** EF1 : le formateur ouvre une session et obtient un code valable 15 minutes (RG1). */
	@Transactional
	public Session ouvrir(Long formateurId) {
		OffsetDateTime maintenant = OffsetDateTime.now();
		Session session = new Session(formateurId, genererCode(), maintenant, maintenant.plus(DUREE_CODE));
		return sessions.save(session);
	}

	/** EF2 : l'etudiant marque sa presence avec un code valide. */
	@Transactional
	public Presence marquerPresence(String code, Long etudiantId) {
		OffsetDateTime maintenant = OffsetDateTime.now();
		Session session = sessions.findByCode(normaliser(code))
				.orElseThrow(() -> new RegleMetierException(CodeErreur.CODE_INCONNU, "Code de presence inconnu"));

		if (session.isCloturee()) {
			throw new RegleMetierException(CodeErreur.SESSION_CLOTUREE, "Session cloturee");
		}
		if (session.codeExpire(maintenant)) {
			throw new RegleMetierException(CodeErreur.CODE_EXPIRE, "Code de presence expire");
		}

		TentativeCode tentative = tentatives.findById(session.getId()).orElseGet(() -> new TentativeCode(session.getId(), etudiantId));
		if (tentative.estBloque(maintenant)) {
			throw new RegleMetierException(CodeErreur.QUOTA_TENTATIVES, "Trop de tentatives erronees, reessayez plus tard");
		}

		if (presences.findBySessionIdAndEtudiantId(session.getId(), etudiantId).isPresent()) {
			throw new RegleMetierException(CodeErreur.DEJA_PRESENT, "Presence deja marquee sur cette session");
		}

		Presence presence = presences.save(new Presence(session, etudiantId, maintenant, SourcePresence.ETUDIANT));
		tentative.reinitialiser();
		tentatives.save(tentative);
		return presence;
	}

	/** EF11 : le formateur ajoute une presence manuelle (source FORMATEUR, RG12). */
	@Transactional
	public Presence ajouterPresenceManuelle(Long sessionId, Long etudiantId) {
		Session session = sessions.findById(sessionId)
				.orElseThrow(() -> new RegleMetierException(CodeErreur.INTROUVABLE, "Session inconnue"));
		if (presences.findBySessionIdAndEtudiantId(sessionId, etudiantId).isPresent()) {
			throw new RegleMetierException(CodeErreur.DEJA_PRESENT, "Presence deja marquee sur cette session");
		}
		return presences.save(new Presence(session, etudiantId, OffsetDateTime.now(), SourcePresence.FORMATEUR));
	}

	/** EF12 : cloture explicite ; bloque tout depot et toute relecture ulterieure (RG10). */
	@Transactional
	public Session cloturer(Long sessionId) {
		Session session = sessions.findById(sessionId)
				.orElseThrow(() -> new RegleMetierException(CodeErreur.INTROUVABLE, "Session inconnue"));
		session.cloturer();
		return session;
	}

	private String normaliser(String code) {
		return code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
	}

	private String genererCode() {
		StringBuilder sb = new StringBuilder(8);
		for (int i = 0; i < 8; i++) {
			sb.append(ALPHABET.charAt(ALEATOIRE.nextInt(ALPHABET.length())));
		}
		return sb.toString();
	}
}
