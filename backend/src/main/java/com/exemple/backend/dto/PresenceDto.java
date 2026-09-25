package com.exemple.backend.dto;

import com.exemple.backend.entity.SourcePresence;
import java.time.OffsetDateTime;

/** Presence telle que vue dans le tableau de bord formateur (EF10). */
public record PresenceDto(
		Long id,
		Long sessionId,
		Long etudiantId,
		OffsetDateTime horodatage,
		SourcePresence source) {
}
