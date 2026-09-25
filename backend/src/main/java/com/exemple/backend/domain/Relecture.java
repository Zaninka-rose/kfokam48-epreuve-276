package com.exemple.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/**
 * Relecture d'un exercice par un pair. L'identite du relecteur ne doit jamais
 * etre exposee a l'auteur (RG6) : c'est la couche DTO qui l'elide.
 * Une relecture rendue est definitive (RG8).
 */
@Entity
@Table(name = "relecture")
public class Relecture {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "exercice_id", nullable = false, unique = true)
	private Exercice exercice;

	@Column(name = "relecteur_id", nullable = false)
	private Long relecteurId;

	@Column(nullable = false)
	private int note;

	@Column(nullable = false, length = 2000)
	private String commentaire;

	@Column(name = "rendue_le", nullable = false)
	private OffsetDateTime rendueLe;

	protected Relecture() {
		// JPA
	}

	public Relecture(Exercice exercice, Long relecteurId, int note, String commentaire, OffsetDateTime rendueLe) {
		this.exercice = exercice;
		this.relecteurId = relecteurId;
		this.note = note;
		this.commentaire = commentaire;
		this.rendueLe = rendueLe;
	}

	public Long getId() {
		return id;
	}

	public Exercice getExercice() {
		return exercice;
	}

	public Long getRelecteurId() {
		return relecteurId;
	}

	public int getNote() {
		return note;
	}

	public String getCommentaire() {
		return commentaire;
	}

	public OffsetDateTime getRendueLe() {
		return rendueLe;
	}
}
