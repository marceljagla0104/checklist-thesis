import { NgModule } from '@angular/core';

import { TranslateModule } from '@ngx-translate/core';
import { CommonModule } from '@angular/common';
import { routing } from './admin.routing';
import { AdminPage } from './pages';
import { FormsModule } from '@angular/forms';
import { SharedModule } from '../shared/shared.module';
import { AdminService } from './services';
import { DocumentationTableComponent } from './components';
import { MatTableModule } from '@angular/material/table';

@NgModule({
  declarations: [AdminPage, DocumentationTableComponent],
  imports: [
    TranslateModule,
    CommonModule,
    FormsModule,
    routing,
    SharedModule,
    MatTableModule,
  ],
  providers: [AdminService],
  exports: [],
})
export class AdminModule {}
