package com.exemple.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "session")
public class Session {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long formateurId;

	@Column(nullable = false, unique = true, length = 10)
	private String code;

	@Column(name = "ouverture", nullable = false)
	private OffsetDateTime ouverture;

	@Column(name = "expiration_code", nullable = false)
	private OffsetDateTime expirationCode;

	@Column(nullable = false)
	private boolean cloturee;

	protected Session() {
		// JPA
	}

	public Session(Long formateurId, String code, OffsetDateTime ouverture, OffsetDateTime expirationCode) {
		this.formateurId = formateurId;
		this.code = code;
		this.ouverture = ouverture;
		this.expirationCode = expirationCode;
		this.cloturee = false;
	}

	public boolean codeExpire(OffsetDateTime maintenant) {
		return maintenant.isAfter(expirationCode);
	}

	public void cloturer() {
		this.cloturee = true;
	}

	public Long getId() {
		return id;
	}

	public Long getFormateurId() {
		return formateurId;
	}

	public String getCode() {
		return code;
	}

	public OffsetDateTime getOuverture() {
		return ouverture;
	}

	public OffsetDateTime getExpirationCode() {
		return expirationCode;
	}

	public boolean isCloturee() {
		return cloturee;
	}
}
