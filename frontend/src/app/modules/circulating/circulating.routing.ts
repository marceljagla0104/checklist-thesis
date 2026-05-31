import { ModuleWithProviders } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CirculatingPage } from './pages';

const routes: Routes = [{ path: '', component: CirculatingPage }];

export const routing: ModuleWithProviders<RouterModule> =
  RouterModule.forChild(routes);
