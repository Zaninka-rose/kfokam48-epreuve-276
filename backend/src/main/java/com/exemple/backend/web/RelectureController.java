package com.exemple.backend.web;

import com.exemple.backend.domain.Relecture;
import com.exemple.backend.service.RelectureService;
import com.exemple.backend.web.dto.ExerciceDto;
import com.exemple.backend.web.dto.LigneTableauDto;
import com.exemple.backend.web.dto.RelectureInput;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints relectures (EF8) et tableau de bord formateur (EF10).
 */
@RestController
@RequestMapping("/api")
public class RelectureController {

	private final RelectureService relectureService;

	public RelectureController(RelectureService relectureService) {
		this.relectureService = relectureService;
	}

	@PostMapping("/relectures/{exerciceId}")
	public ResponseEntity<ResultatLight> rendre(
			@PathVariable Long exerciceId,
			@RequestHeader("X-Relecteur-Id") Long relecteurId,
			@Valid @RequestBody RelectureInput input) {
		Relecture rendue = relectureService.rendre(exerciceId, relecteurId, input.note(), input.commentaire());
		return ResponseEntity.ok(new ResultatLight(rendue.getExercice().getId(), rendue.getNote(), rendue.getRendueLe()));
	}

	@GetMapping("/sessions/{id}/tableau")
	public ResponseEntity<List<LigneTableauDto>> tableau(@PathVariable Long id) {
		List<LigneTableauDto> lignes = relectureService.tableau(id).stream()
				.map(l -> new LigneTableauDto(l.etudiantId(), l.nom(), l.presences(),
						l.exercices().stream().map(e -> ExerciceController.toDto(e, relecteurDe(e.getId()))).toList(),
						l.moyenne(), l.relecturesEnAttente()))
				.toList();
		return ResponseEntity.ok(lignes);
	}

	private Long relecteurDe(Long exerciceId) {
		// Visible uniquement dans le tableau formateur, jamais a l'auteur (RG6).
		return relectureService.relecteurDe(exerciceId);
	}

	public record ResultatLight(Long exerciceId, int note, java.time.OffsetDateTime rendueLe) {
	}
}
