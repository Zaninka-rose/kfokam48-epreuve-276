import { Routes } from '@angular/router';

import { OuvrirSession } from './ouvrir-session/ouvrir-session';
import { TableauBord } from './tableau-bord/tableau-bord';

export const routes: Routes = [
	{
		path: 'ouvrir-session',
		component: OuvrirSession,
	},
	{
		path: 'tableau-bord',
		component: TableauBord,
	},
];
