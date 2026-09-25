package com.exemple.backend.web.dto;

import java.time.OffsetDateTime;

/**
 * Resultat d'une relecture tel que l'auteur le consulte (EF9/RG6) :
 * note + commentaire, JAMAIS l'identite du relecteur.
 */
public record ResultatDto(
		Long exerciceId,
		int note,
		String commentaire,
		OffsetDateTime rendueLe) {
}
