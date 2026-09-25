package com.exemple.backend.web.dto;

import java.time.OffsetDateTime;

/** Session renvoyee au formateur apres ouverture (EF1). */
public record SessionDto(
		Long id,
		Long formateurId,
		String code,
		OffsetDateTime ouverture,
		OffsetDateTime expirationCode,
		boolean cloturee) {
}
