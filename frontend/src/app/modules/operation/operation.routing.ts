import { ModuleWithProviders } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ChecklistPage } from './pages';

const routes: Routes = [{ path: '', component: ChecklistPage }];

export const routing: ModuleWithProviders<RouterModule> =
  RouterModule.forChild(routes);
