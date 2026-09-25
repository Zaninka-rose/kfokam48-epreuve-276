import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
	afterEach,
	beforeEach,
	describe,
	expect,
	it,
	vi,
} from 'vitest';

import { ApiError } from '../api/client';
import { OuvrirSession } from './ouvrir-session';

describe('OuvrirSession', () => {
	let fixture: ComponentFixture<OuvrirSession>;
	let el: HTMLElement;
	let fetchMock: ReturnType<typeof vi.fn>;

	beforeEach(async () => {
		fetchMock = vi.fn();
		vi.stubGlobal('fetch', fetchMock);

		await TestBed.configureTestingModule({
			imports: [OuvrirSession],
		}).compileComponents();

		fixture = TestBed.createComponent(OuvrirSession);
		await fixture.whenStable();
		el = fixture.nativeElement;
	});

	afterEach(() => {
		vi.unstubAllGlobals();
		vi.restoreAllMocks();
	});

	function saisitFormateur(id: string): void {
		const input = el.querySelector<HTMLInputElement>('#formateurId')!;
		input.value = id;
		input.dispatchEvent(new Event('input'));
	}

	async function soumetFormulaire(): Promise<void> {
		el.querySelector<HTMLFormElement>('form')!.dispatchEvent(
			new Event('submit', { cancelable: true }),
		);
	}

	async function attendreAffichage(contenu: string): Promise<void> {
		await vi.waitFor(() => {
			expect(el.textContent).toContain(contenu);
		});
	}

	it('affiche le code et l’expiration après une ouverture 201 (critère 1)', async () => {
		fetchMock.mockResolvedValue(
			new Response(
				JSON.stringify({
					id: 42,
					formateurId: 1,
					code: 'AB12-CD34',
					ouverture: '2026-09-25T10:00:00Z',
					expirationCode: '2026-09-25T10:15:00Z',
					cloturee: false,
				}),
				{ status: 201 },
			),
		);

		saisitFormateur('1');
		await soumetFormulaire();
		await attendreAffichage('Session n°42 ouverte');

		const texte = el.textContent ?? '';
		expect(texte).toContain('AB12-CD34');
		expect(texte).toContain('2026-09-25T10:15:00Z');
		expect(fetchMock).toHaveBeenCalledWith(
			'/api/sessions',
			expect.objectContaining({ method: 'POST' }),
		);
		expect(JSON.parse(fetchMock.mock.calls[0][1].body)).toEqual({ formateurId: 1 });
	});

	it('affiche une erreur réseau quand le backend est injoignable (critère 2)', async () => {
		fetchMock.mockRejectedValue(new TypeError('network down'));

		saisitFormateur('1');
		await soumetFormulaire();
		await attendreAffichage('Backend injoignable (réseau)');
	});

	it('affiche une erreur HTTP quand l’API refuse (critère 3)', async () => {
		fetchMock.mockResolvedValue(
			new Response(
				JSON.stringify({ status: 400, code: null, message: 'validation' }),
				{ status: 400 },
			),
		);

		saisitFormateur('1');
		await soumetFormulaire();
		await attendreAffichage('Ouverture refusée (HTTP 400)');
	});

	it('valide la saisie côté client : identifiant non numérique refusé sans appel API', async () => {
		saisitFormateur('abc');
		await soumetFormulaire();
		await attendreAffichage("Saisissez l'identifiant numérique du formateur.");

		expect(fetchMock).not.toHaveBeenCalled();
	});
});
