package com.exemple.backend.exception;

import com.exemple.backend.service.RegleMetierException;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.ResponseEntity;

/**
 * Gestion centralisee des erreurs (B4) : toute reponse en echec respecte
 * l'enveloppe { status, code, message, timestamp } du contrat (ENF3).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	public record ErrorDto(int status, String code, String message, String timestamp, List<String> details) {
	}

	@ExceptionHandler(RegleMetierException.class)
	public ResponseEntity<ErrorDto> regleMetier(RegleMetierException ex) {
		return ResponseEntity.status(ex.getCodeErreur().httpStatus())
				.body(new ErrorDto(ex.getCodeErreur().httpStatus(), ex.getCodeErreur().name(),
						ex.getMessage(), OffsetDateTime.now().toString(), null));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorDto> validation(MethodArgumentNotValidException ex) {
		List<String> details = ex.getBindingResult().getFieldErrors().stream()
				.map(GlobalExceptionHandler::decrire)
				.toList();
		return ResponseEntity.badRequest()
				.body(new ErrorDto(HttpStatus.BAD_REQUEST.value(), null, "Requete invalide",
						OffsetDateTime.now().toString(), details));
	}

	@ExceptionHandler({ MissingRequestHeaderException.class, MissingServletRequestParameterException.class })
	public ResponseEntity<ErrorDto> requeteIncomplete(Exception ex) {
		return ResponseEntity.badRequest()
				.body(new ErrorDto(HttpStatus.BAD_REQUEST.value(), null, ex.getMessage(),
						OffsetDateTime.now().toString(), null));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorDto> inattendue(Exception ex) {
		// B4 : aucune donnée sensible (stack trace) ne fuit vers le client.
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ErrorDto(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, "Erreur interne",
						OffsetDateTime.now().toString(), null));
	}

	private static String decrire(FieldError fieldError) {
		return fieldError.getField() + " : " + fieldError.getDefaultMessage();
	}
}
