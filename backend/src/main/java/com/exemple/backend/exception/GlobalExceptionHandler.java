package com.exemple.backend.exception;

import com.exemple.backend.service.RegleMetierException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Gestion centralisee des erreurs (B4) : toute reponse en echec respecte
 * l'enveloppe { status, message, timestamp } du contrat (ENF3).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	public record ErrorDto(int status, String message, String timestamp, List<String> details) {
	}

	@ExceptionHandler(RegleMetierException.class)
	public ResponseEntity<ErrorDto> regleMetier(RegleMetierException ex) {
		return ResponseEntity.status(ex.getCodeErreur().httpStatus())
				.body(erreur(ex.getCodeErreur().httpStatus(), ex.getMessage(), null));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorDto> validation(MethodArgumentNotValidException ex) {
		List<String> details = ex.getBindingResult().getFieldErrors().stream()
				.map(GlobalExceptionHandler::decrire)
				.toList();
		return ResponseEntity.badRequest()
				.body(erreur(HttpStatus.BAD_REQUEST.value(), "Requete invalide", details));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorDto> inattendue(Exception ex) {
		// B4 : aucune donnée sensible (stack trace) ne fuit vers le client.
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(erreur(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Erreur interne", null));
	}

	private static String decrire(FieldError fieldError) {
		return fieldError.getField() + " : " + fieldError.getDefaultMessage();
	}

	private static ErrorDto erreur(int status, String message, List<String> details) {
		Map<String, List<String>> avecDetails = details == null || details.isEmpty() ? Map.of() : Map.of("details", details);
		return new ErrorDto(status, message, OffsetDateTime.now().toString(), avecDetails.get("details"));
	}
}
