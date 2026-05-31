import { NgModule } from '@angular/core';

import { TaskTableComponent } from './components';
import { CirculatingPage } from './pages';
import { TaskService } from './services';
import { TranslateModule } from '@ngx-translate/core';
import { SharedModule } from '../shared/shared.module';
import { MatTableModule } from '@angular/material/table';
import { CommonModule } from '@angular/common';
import { routing } from './circulating.routing';

const COMPONENTS = [TaskTableComponent];

const PAGES = [CirculatingPage];

const SERVICES = [TaskService];

@NgModule({
  declarations: [...COMPONENTS, ...PAGES],
  imports: [
    SharedModule,
    TranslateModule,
    MatTableModule,
    CommonModule,
    routing,
  ],
  providers: [[...SERVICES]],
})
export class CirculatingModule {}
