import { NgModule } from '@angular/core';

import { TranslateModule } from '@ngx-translate/core';
import { SharedModule } from '../shared/shared.module';
import { MatTableModule } from '@angular/material/table';
import { CommonModule } from '@angular/common';
import { routing } from './operation.routing';
import { ChecklistPage } from './pages';
import {
  AudioNoteComponent,
  BoundaryEventComponent,
  CallCirculatingComponent,
  CameraComponent,
  CheckboxComponent,
  CirculatingTaskComponent,
  ImageComponent,
  LanguageComponent,
  ParallelComponent,
  PathComponent,
  StartComponent,
  SubprocessComponent,
  SubprocessTabComponent,
  TextComponent,
  TextNoteComponent,
  TimestampComponent,
  UploadComponent,
  XorComponent,
} from './components';
import { OperationService } from './services';
import { MatSidenavModule } from '@angular/material/sidenav';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatTabsModule } from '@angular/material/tabs';
import { MatStepperModule } from '@angular/material/stepper';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { TextFieldModule } from '@angular/cdk/text-field';
import { WebcamModule } from 'ngx-webcam';
import { DocumentationModule } from '../documentation/documentation.module';
import { CameraDialog } from './dialogs';
import { MatDialogModule } from '@angular/material/dialog';

const COMPONENTS = [
  SubprocessComponent,
  SubprocessTabComponent,
  PathComponent,
  XorComponent,
  ParallelComponent,
  StartComponent,
  TextComponent,
  CheckboxComponent,
  UploadComponent,
  BoundaryEventComponent,
  CameraComponent,
  ImageComponent,
  AudioNoteComponent,
  TextNoteComponent,
  LanguageComponent,
  TimestampComponent,
  CirculatingTaskComponent,
  CallCirculatingComponent,
];

const PAGES = [ChecklistPage];

const SERVICES = [OperationService];
const DIALOGS = [CameraDialog];

@NgModule({
  declarations: [...PAGES, ...COMPONENTS, ...DIALOGS],
  imports: [
    SharedModule,
    DocumentationModule,
    TranslateModule,
    CommonModule,

    MatSidenavModule,
    HttpClientModule,
    ReactiveFormsModule,
    MatTabsModule,
    MatStepperModule,
    MatIconModule,
    MatButtonModule,
    MatSnackBarModule,
    TextFieldModule,
    FormsModule,
    WebcamModule,
    MatTableModule,
    routing,
    MatDialogModule,
  ],
  providers: [[...SERVICES]],
  exports: [[]],
})
export class OperationModule {}
