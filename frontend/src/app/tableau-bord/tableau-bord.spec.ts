import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
	afterEach,
	beforeEach,
	describe,
	expect,
	it,
	vi,
} from 'vitest';

import { TableauBord } from './tableau-bord';

describe('TableauBord', () => {
	let fixture: ComponentFixture<TableauBord>;
	let el: HTMLElement;
	let fetchMock: ReturnType<typeof vi.fn>;

	beforeEach(async () => {
		fetchMock = vi.fn();
		vi.stubGlobal('fetch', fetchMock);

		await TestBed.configureTestingModule({
			imports: [TableauBord],
		}).compileComponents();

		fixture = TestBed.createComponent(TableauBord);
		await fixture.whenStable();
		el = fixture.nativeElement;
	});

	afterEach(() => {
		vi.unstubAllGlobals();
		vi.restoreAllMocks();
	});

	function saisitSession(id: string): void {
		const input = el.querySelector<HTMLInputElement>('#sessionId')!;
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

	const lignes = () =>
		new Response(
			JSON.stringify([
				{
					etudiantId: 1,
					nom: 'Alice K.',
					presences: 1,
					exercices: [
						{
							id: 10,
							sessionId: 5,
							auteurId: 1,
							lien: 'https://exemples.org/ex10',
							statut: 'EN_ATTENTE_RELECTURE',
							deposeLe: '2026-09-25T10:05:00Z',
							relecteurId: 2,
						},
					],
					moyenne: null,
					relecturesEnAttente: 1,
				},
				{
					etudiantId: 2,
					nom: 'Brice M.',
					presences: 1,
					exercices: [],
					moyenne: 15.5,
					relecturesEnAttente: 0,
				},
			]),
			{ status: 200 },
		);

	it('affiche les lignes par étudiant : présences, exercices, moyenne, relectures en attente (critère 1)', async () => {
		fetchMock.mockResolvedValue(lignes());

		saisitSession('5');
		await soumetFormulaire();
		await attendreAffichage('Alice K.');

		const texte = el.textContent ?? '';
		expect(texte).toContain('Brice M.');
		expect(texte).toContain('Exercice n°10');
		expect(texte).toContain('En attente de relecture');
		expect(texte).toContain('15.50');
		expect(texte).toContain('Aucun');
		expect(fetchMock).toHaveBeenCalledWith(
			'/api/sessions/5/tableau',
			expect.objectContaining({
				headers: expect.objectContaining({ 'Content-Type': 'application/json' }),
			}),
		);
	});

	it('affiche « — » pour un étudiant sans relecture rendue (moyenne null, RG9)', async () => {
		fetchMock.mockResolvedValue(lignes());

		saisitSession('5');
		await soumetFormulaire();
		await attendreAffichage('Alice K.');

		const ligneAlice = Array.from(el.querySelectorAll('tbody tr')).find(
			(tr) => tr.textContent?.includes('Alice K.'),
		)!;
		expect(ligneAlice.textContent).toContain('—');
	});

	it('affiche une erreur quand la session est inconnue (404)', async () => {
		fetchMock.mockResolvedValue(
			new Response(
				JSON.stringify({ status: 404, code: 'INTROUVABLE', message: 'session' }),
				{ status: 404 },
			),
		);

		saisitSession('999');
		await soumetFormulaire();
		await attendreAffichage('Tableau indisponible (HTTP 404)');
	});

	it('affiche une erreur réseau quand le backend est injoignable', async () => {
		fetchMock.mockRejectedValue(new TypeError('network down'));

		saisitSession('5');
		await soumetFormulaire();
		await attendreAffichage('Backend injoignable (réseau)');
	});

	it('valide la saisie côté client : identifiant non numérique refusé sans appel API', async () => {
		saisitSession('abc');
		await soumetFormulaire();
		await attendreAffichage('Saisissez l’identifiant numérique de la session.');

		expect(fetchMock).not.toHaveBeenCalled();
	});
});
