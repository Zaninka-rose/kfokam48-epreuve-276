package com.exemple.backend.web;

import com.exemple.backend.domain.Relecture;
import com.exemple.backend.service.RelectureService;
import com.exemple.backend.web.dto.ExerciceInput;
import com.exemple.backend.web.dto.ResultatDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints exercices (EF5, EF6, EF9). L'identite de l'etudiant transite par
 * l'en-tete X-Etudiant-Id en attendant le mecanisme d'identification final ;
 * aucune regle metier n'est duplique cote client (F2/F3).
 */
@RestController
@RequestMapping("/api/exercices")
public class ExerciceController {

	private final RelectureService relectureService;

	public ExerciceController(RelectureService relectureService) {
		this.relectureService = relectureService;
	}

	public record DepotInput(@NotNull Long sessionId, @Valid ExerciceInput exercice) {
	}

	public record RelectureBody(@NotNull Long relecteurId) {
	}

	@PostMapping
	public ResponseEntity<com.exemple.backend.web.dto.ExerciceDto> deposer(
			@RequestHeader("X-Etudiant-Id") Long etudiantId,
			@Valid @RequestBody DepotInput input) {
		var exercice = relectureService.deposer(input.sessionId(), etudiantId, input.exercice().lien());
		return ResponseEntity.status(HttpStatus.CREATED).body(toDto(exercice, null));
	}

	@PutMapping("/{id}")
	public ResponseEntity<com.exemple.backend.web.dto.ExerciceDto> remplacer(
			@PathVariable Long id,
			@RequestHeader("X-Etudiant-Id") Long etudiantId,
			@Valid @RequestBody ExerciceInput input) {
		var exercice = relectureService.remplacerLien(id, etudiantId, input.lien());
		return ResponseEntity.ok(toDto(exercice, null));
	}

	@GetMapping("/{id}/resultat")
	public ResponseEntity<ResultatDto> resultat(
			@PathVariable Long id,
			@RequestHeader("X-Etudiant-Id") Long etudiantId) {
		Relecture relecture = relectureService.resultat(id, etudiantId);
		if (relecture == null || relecture.getNote() == 0 && relecture.getCommentaire().isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		// RG6 : le relecteurId n'est volontairement pas projete dans la reponse.
		return ResponseEntity.ok(new ResultatDto(id, relecture.getNote(), relecture.getCommentaire(), relecture.getRendueLe()));
	}

	static com.exemple.backend.web.dto.ExerciceDto toDto(com.exemple.backend.domain.Exercice exercice, Long relecteurId) {
		return new com.exemple.backend.web.dto.ExerciceDto(exercice.getId(), exercice.getSession().getId(),
				exercice.getAuteurId(), exercice.getLien(), exercice.getStatut(), exercice.getDeposeLe(), relecteurId);
	}
}
