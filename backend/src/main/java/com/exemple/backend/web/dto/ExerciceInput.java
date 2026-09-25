package com.exemple.backend.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Corps de depot ou de remplacement d'un exercice (EF5, EF6).
 * La validation des entrees est une contrainte B4 du cahier des charges.
 */
public record ExerciceInput(
		@NotBlank @Size(max = 500) String lien) {
}
