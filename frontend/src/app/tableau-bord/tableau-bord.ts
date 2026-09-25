import { Component, signal } from '@angular/core';

import { api, ApiError, type LigneTableau } from '../api/client';

type Etat = 'formulaire' | 'en_cours' | 'affiche' | 'erreur';

@Component({
	selector: 'app-tableau-bord',
	templateUrl: './tableau-bord.html',
	styleUrl: './tableau-bord.css',
})
export class TableauBord {
	protected readonly etat = signal<Etat>('formulaire');
	protected readonly sessionId = signal('');
	protected readonly erreur = signal('');
	protected readonly lignes = signal<LigneTableau[]>([]);

	protected async consulter(event: Event): Promise<void> {
		event.preventDefault();
		const id = Number(this.sessionId());
		if (!Number.isInteger(id) || id <= 0) {
			this.erreur.set('Saisissez l’identifiant numérique de la session.');
			return;
		}

		this.etat.set('en_cours');
		this.erreur.set('');
		try {
			this.lignes.set(await api.tableau(id));
			this.etat.set('affiche');
		} catch (e) {
			this.etat.set('erreur');
			this.erreur.set(
				e instanceof ApiError
					? `Tableau indisponible (HTTP ${e.status})`
					: 'Backend injoignable (réseau)',
			);
		}
	}

	protected libelleStatut(
		statut: 'EN_ATTENTE_ASSIGNATION' | 'EN_ATTENTE_RELECTURE' | 'RELUE',
	): string {
		switch (statut) {
			case 'EN_ATTENTE_ASSIGNATION':
				return 'En attente d’assignation';
			case 'EN_ATTENTE_RELECTURE':
				return 'En attente de relecture';
			case 'RELUE':
				return 'Relue';
		}
	}

	protected formatMoyenne(moyenne: number | null): string {
		return moyenne === null ? '—' : moyenne.toFixed(2);
	}
}
