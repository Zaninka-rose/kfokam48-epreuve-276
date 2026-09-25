package com.exemple.backend.domain;

public enum StatutExercice {
	/** Aucun relecteur eligible encore designe (RG13). */
	EN_ATTENTE_ASSIGNATION,
	/** Un relecteur est designe, la relecture n'est pas encore rendue. */
	EN_ATTENTE_RELECTURE,
	/** Relecture rendue, note et commentaire visibles par l'auteur (RG9). */
	RELUE
}
