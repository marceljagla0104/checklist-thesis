import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { OperationStartPage, StartPage } from './pages';
import { ContainerComponent } from './components';

const routes: Routes = [
  { path: '', component: StartPage },
  { path: 'start', component: OperationStartPage },

  {
    path: '',
    component: ContainerComponent,
    children: [
      {
        path: 'admin',
        loadChildren: () =>
          import('./modules/admin/admin.module').then((m) => m.AdminModule),
      },
      {
        path: 'checklist/:id',
        loadChildren: () =>
          import('./modules/operation/operation.module').then(
            (m) => m.OperationModule,
          ),
      },
      {
        path: 'circulating',
        loadChildren: () =>
          import('./modules/circulating/circulating.module').then(
            (m) => m.CirculatingModule,
          ),
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRouting {}
