import { NgModule } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';

import { MatTableModule } from '@angular/material/table';
import { CommonModule } from '@angular/common';
import {
  ChronologicalEditorComponent,
  DurationEditorComponent,
  OverviewComponent,
} from './components';
import { DocumentationService, DownloadService } from './services';
import { SharedModule } from '../shared/shared.module';

const COMPONENTS = [
  ChronologicalEditorComponent,
  DurationEditorComponent,
  OverviewComponent,
];

const SERVICES = [DocumentationService, DownloadService];

@NgModule({
  declarations: [...COMPONENTS],
  imports: [SharedModule, TranslateModule, MatTableModule, CommonModule],
  providers: [[SERVICES]],
  exports: [...COMPONENTS],
})
export class DocumentationModule {}
