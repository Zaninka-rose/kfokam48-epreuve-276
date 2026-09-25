import { Component, signal } from '@angular/core';
import { api, ApiError } from './api/client';

@Component({
	selector: 'app-root',
	templateUrl: './app.html',
	styleUrl: './app.css',
})
export class App {
	protected readonly statut = signal<'en_cours' | 'ok' | 'ko'>('en_cours');
	protected readonly message = signal('');

	constructor() {
		this.verifierBackend();
	}

	protected async verifierBackend(): Promise<void> {
		try {
			const ping = await api.ping();
			this.statut.set('ok');
			this.message.set(`Backend « ${ping.service} » joignable (statut ${ping.status})`);
		} catch (erreur) {
			this.statut.set('ko');
			this.message.set(
				erreur instanceof ApiError
					? `Backend injoignable (HTTP ${erreur.status})`
					: 'Backend injoignable (réseau)',
			);
		}
	}
}
