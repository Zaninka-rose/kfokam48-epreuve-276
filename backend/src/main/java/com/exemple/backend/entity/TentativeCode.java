package com.exemple.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/**
 * Compteur de tentatives de code erronees consecutives, par (session, etudiant).
 * RG3 : 5 erreurs consecutives => blocage de 2 minutes. Remis a zero au succes.
 */
@Entity
@Table(name = "tentative_code")
public class TentativeCode {

	public static final int MAX_ERREURS = 5;
	public static final java.time.Duration DUREE_BLOCAGE = java.time.Duration.ofMinutes(2);

	@Id
	@Column(name = "session_id")
	private Long sessionId;

	@Column(name = "etudiant_id", nullable = false)
	private Long etudiantId;

	@Column(name = "erreurs_consecutives", nullable = false)
	private int erreursConsecutives;

	@Column(name = "bloque_jusqua")
	private OffsetDateTime bloqueJusqua;

	protected TentativeCode() {
		// JPA
	}

	public TentativeCode(Long sessionId, Long etudiantId) {
		this.sessionId = sessionId;
		this.etudiantId = etudiantId;
		this.erreursConsecutives = 0;
	}

	public boolean estBloque(OffsetDateTime maintenant) {
		return bloqueJusqua != null && maintenant.isBefore(bloqueJusqua);
	}

	/** Enregistre une erreur de code et declenche le blocage a la 5e consecutive. */
	public void noterErreur(OffsetDateTime maintenant) {
		erreursConsecutives++;
		if (erreursConsecutives >= MAX_ERREURS) {
			bloqueJusqua = maintenant.plus(DUREE_BLOCAGE);
		}
	}

	public void reinitialiser() {
		erreursConsecutives = 0;
		bloqueJusqua = null;
	}

	public Long getSessionId() {
		return sessionId;
	}

	public Long getEtudiantId() {
		return etudiantId;
	}

	public int getErreursConsecutives() {
		return erreursConsecutives;
	}

	public OffsetDateTime getBloqueJusqua() {
		return bloqueJusqua;
	}
}
