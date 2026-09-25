import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { api, ApiError, type Session } from '../api/client';

type Etat = 'formulaire' | 'en_cours' | 'ouverte' | 'erreur';

@Component({
	selector: 'app-ouvrir-session',
	templateUrl: './ouvrir-session.html',
	styleUrl: './ouvrir-session.css',
})
export class OuvrirSession {
	protected readonly etat = signal<Etat>('formulaire');
	protected readonly formateurId = signal('');
	protected readonly erreur = signal('');
	protected readonly session = signal<Session | null>(null);

	protected async soumettre(event: Event): Promise<void> {
		event.preventDefault();
		const id = Number(this.formateurId());
		if (!Number.isInteger(id) || id <= 0) {
			this.erreur.set("Saisissez l'identifiant numérique du formateur.");
			return;
		}

		this.etat.set('en_cours');
		this.erreur.set('');
		try {
			const session = await api.ouvrirSession({ formateurId: id });
			this.session.set(session);
			this.etat.set('ouverte');
		} catch (e) {
			this.etat.set('erreur');
			this.erreur.set(
				e instanceof ApiError
					? `Ouverture refusée (HTTP ${e.status})`
					: 'Backend injoignable (réseau)',
			);
		}
	}
}
