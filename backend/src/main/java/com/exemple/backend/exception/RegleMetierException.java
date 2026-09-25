package com.exemple.backend.exception;

/**
 * Exception metier portant un code de l'enveloppe d'erreur homogene (ENF3/B4).
 * Levee par les services, traduite en reponse HTTP par GlobalExceptionHandler.
 */
public class RegleMetierException extends RuntimeException {

	private final CodeErreur codeErreur;

	public RegleMetierException(CodeErreur codeErreur, String message) {
		super(message);
		this.codeErreur = codeErreur;
	}

	public CodeErreur getCodeErreur() {
		return codeErreur;
	}
}
