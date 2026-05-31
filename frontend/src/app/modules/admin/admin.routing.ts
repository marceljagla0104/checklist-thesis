import { ModuleWithProviders } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AdminPage } from './pages';

const routes: Routes = [{ path: '', component: AdminPage }];

export const routing: ModuleWithProviders<RouterModule> =
  RouterModule.forChild(routes);
