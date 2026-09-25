package com.exemple.backend.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Corps d'une relecture envoyee par le relecteur (EF8).
 * RG7 : note entiere entre 0 et 20. RG8 : definitive.
 */
public record RelectureInput(
		@NotNull @Min(0) @Max(20) Integer note,
		@NotBlank @Size(max = 2000) String commentaire) {
}
