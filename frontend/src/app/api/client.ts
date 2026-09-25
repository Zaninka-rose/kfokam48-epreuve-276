/**
 * Couche d'appel API dédiée (F2) : tout appel HTTP du frontend passe par ce
 * fichier. Aucun fetch dispersé ailleurs dans le code, même au stade squelette.
 *
 * En dev, les appels /api sont proxifiés vers http://localhost:8080
 * (voir proxy.conf.json).
 */

const BASE = '/api';

export interface PingResponse {
	status: string;
	service: string;
}

/** Corps de POST /api/sessions (EF1) — voir SessionOuvertureInput du contrat. */
export interface OuvrirSessionInput {
	formateurId: number;
}

/** Session renvoyée par POST /api/sessions — voir schema Session du contrat. */
export interface Session {
	id: number;
	formateurId: number;
	/** Code de présence généré, valable 15 minutes (RG1). */
	code: string;
	ouverture: string;
	expirationCode: string;
	cloturee: boolean;
}

export class ApiError extends Error {
	constructor(
		public readonly status: number,
		public readonly payload: unknown,
	) {
		super(`Appel API échoué (${status})`);
		this.name = 'ApiError';
	}
}

async function request<T>(chemin: string, init?: RequestInit): Promise<T> {
	const reponse = await fetch(`${BASE}${chemin}`, {
		headers: { 'Content-Type': 'application/json', ...init?.headers },
		...init,
	});
	if (!reponse.ok) {
		throw new ApiError(reponse.status, await reponse.json().catch(() => null));
	}
	return (await reponse.json()) as T;
}

export const api = {
	ping: () => request<PingResponse>('/ping'),
	ouvrirSession: (entree: OuvrirSessionInput) =>
		request<Session>('/sessions', {
			method: 'POST',
			body: JSON.stringify(entree),
		}),
};
