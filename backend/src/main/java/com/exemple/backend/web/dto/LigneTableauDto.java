package com.exemple.backend.web.dto;

import java.util.List;

/**
 * Vue par etudiant du tableau de bord formateur (EF10) :
 * presences, exercices deposes, moyenne, relectures en attente.
 */
public record LigneTableauDto(
		Long etudiantId,
		String nom,
		long presences,
		List<ExerciceDto> exercices,
		Double moyenne,
		long relecturesEnAttente) {
}
