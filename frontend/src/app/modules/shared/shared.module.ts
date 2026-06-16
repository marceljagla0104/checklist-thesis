import { NgModule } from '@angular/core';
import { TimestampPipe } from './pipes';
import { PageComponent } from './pages';
import {
  ButtonComponent,
  LanguageComponent,
  TimestampComponent,
  ToastComponent,
} from './components';

import { SoundSettingsComponent } from '../operation/components/settings/sound/sound.settings.component';
import { SettingsDialog } from './dialogs';
import { MatDialogModule } from '@angular/material/dialog';
import { TranslateModule } from '@ngx-translate/core';
import { MatButtonModule } from '@angular/material/button';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';

const PIPES = [TimestampPipe];
const COMPONENTS = [
  PageComponent,
  ButtonComponent,
  TimestampComponent,
  LanguageComponent,
  ToastComponent,
  SoundSettingsComponent,
];
const DIALOGS = [SettingsDialog];

@NgModule({
  declarations: [...PIPES, ...COMPONENTS, ...DIALOGS],
  imports: [
    MatDialogModule,
    TranslateModule,
    MatButtonModule,
    FormsModule,
    CommonModule,
    MatSelectModule,
    MatInputModule,
  ],
  exports: [...PIPES, ...COMPONENTS, ...DIALOGS],
})
export class SharedModule {}
