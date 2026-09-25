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
@Table(name = "presence", uniqueConstraints = @UniqueConstraint(columnNames = {"session_id", "etudiant_id"}))
public class Presence {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "session_id", nullable = false)
	private Session session;

	@Column(name = "etudiant_id", nullable = false)
	private Long etudiantId;

	@Column(nullable = false)
	private OffsetDateTime horodatage;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private SourcePresence source;

	protected Presence() {
		// JPA
	}

	public Presence(Session session, Long etudiantId, OffsetDateTime horodatage, SourcePresence source) {
		this.session = session;
		this.etudiantId = etudiantId;
		this.horodatage = horodatage;
		this.source = source;
	}

	public Long getId() {
		return id;
	}

	public Session getSession() {
		return session;
	}

	public Long getEtudiantId() {
		return etudiantId;
	}

	public OffsetDateTime getHorodatage() {
		return horodatage;
	}

	public SourcePresence getSource() {
		return source;
	}
}
