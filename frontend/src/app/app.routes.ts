import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';
export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./features/auth/auth').then(m => m.Auth) },
  { path: '', pathMatch: 'full', canActivate: [authGuard], loadComponent: () => import('./features/anagrafiche/anagrafica-list').then(m => m.AnagraficaList) },
  { path: 'anagrafiche/nuova', canActivate: [authGuard], loadComponent: () => import('./features/anagrafiche/anagrafica-form').then(m => m.AnagraficaForm) },
  { path: 'anagrafiche/:id/modifica', canActivate: [authGuard], loadComponent: () => import('./features/anagrafiche/anagrafica-form').then(m => m.AnagraficaForm) },
  { path: '**', redirectTo: '' }
];
