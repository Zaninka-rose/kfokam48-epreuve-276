package com.exemple.backend.web.dto;

import com.exemple.backend.domain.StatutExercice;
import java.time.OffsetDateTime;

/** Exercice tel que vu par le formateur ou l'etudiant (le lien peut etre elide). */
public record ExerciceDto(
		Long id,
		Long sessionId,
		Long auteurId,
		String lien,
		StatutExercice statut,
		OffsetDateTime deposeLe,
		Long relecteurId) {
}
