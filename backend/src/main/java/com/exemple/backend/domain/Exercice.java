package com.exemple.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;

@Entity
@Table(name = "exercice", uniqueConstraints = @UniqueConstraint(columnNames = {"session_id", "auteur_id"}))
public class Exercice {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "session_id", nullable = false)
	private Session session;

	@Column(name = "auteur_id", nullable = false)
	private Long auteurId;

	@Column(nullable = false, length = 500)
	private String lien;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private StatutExercice statut;

	@Column(name = "depose_le", nullable = false)
	private OffsetDateTime deposeLe;

	protected Exercice() {
		// JPA
	}

	public Exercice(Session session, Long auteurId, String lien, OffsetDateTime deposeLe) {
		this.session = session;
		this.auteurId = auteurId;
		this.lien = lien;
		this.deposeLe = deposeLe;
		this.statut = StatutExercice.EN_ATTENTE_ASSIGNATION;
	}

	public void remplacerLien(String nouveauLien) {
		this.lien = nouveauLien;
	}

	public void assignerRelecteur() {
		this.statut = StatutExercice.EN_ATTENTE_RELECTURE;
	}

	public void marquerRelue() {
		this.statut = StatutExercice.RELUE;
	}

	public Long getId() {
		return id;
	}

	public Session getSession() {
		return session;
	}

	public Long getAuteurId() {
		return auteurId;
	}

	public String getLien() {
		return lien;
	}

	public StatutExercice getStatut() {
		return statut;
	}

	public OffsetDateTime getDeposeLe() {
		return deposeLe;
	}
}
