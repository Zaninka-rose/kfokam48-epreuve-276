package com.exemple.backend.domain;

/**
 * Codes d'erreur metier renvoyes dans l'enveloppe "Error" commune
 * (voir CAHIER_DES_CHARGES.md section 4 et le format d'erreur homogene ENF3).
 */
public enum CodeErreur {

	/** RG1/RG2 : code utilise apres l'expiration des 15 minutes. */
	CODE_EXPIRE(410),

	/** EF4/RG2 : l'etudiant est deja marque present sur la session. */
	DEJA_PRESENT(409),

	/** RG3 : 5 tentatives de code erronees consecutives. */
	QUOTA_TENTATIVES(429),

	/** Code de presence inconnu. */
	CODE_INCONNU(404),

	/** EF12/RG10 : session cloturee, plus aucune ecriture possible. */
	SESSION_CLOTUREE(409),

	/** EF6/RG11 : remplacement du lien refuse car une relecture a commence. */
	RELECTURE_DEJA_COMMENCEE(409),

	/** EF8/RG4 : le relecteur est l'auteur de l'exercice. */
	AUTEUR_NON_ELIGIBLE(403),

	/** EF8/RG8 : la relecture est deja rendue, elle est definitive. */
	RELECTURE_DEJA_RENDUE(409),

	/** Ressource introuvable (404 generic). */
	INTROUVABLE(404);

	private final int httpStatus;

	CodeErreur(int httpStatus) {
		this.httpStatus = httpStatus;
	}

	public int httpStatus() {
		return httpStatus;
	}
}
