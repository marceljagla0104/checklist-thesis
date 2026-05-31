import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRouting } from './app.routing';
import { AppComponent, ContainerComponent } from './components';
import { OperationStartPage, StartPage } from './pages';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { SharedModule } from './modules/shared/shared.module';
import { MatIconModule } from '@angular/material/icon';
import { OperationModule } from './modules/operation/operation.module';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { DocumentationModule } from './modules/documentation/documentation.module';

const COMPONENTS = [AppComponent, ContainerComponent];

const PAGES = [StartPage, OperationStartPage];

const MAT_MODULES = [MatButtonModule, MatIconModule, MatDialogModule];

@NgModule({
  declarations: [...COMPONENTS, ...PAGES],
  imports: [
    BrowserModule,
    AppRouting,
    BrowserAnimationsModule,
    HttpClientModule,
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: HttpLoaderFactory,
        deps: [HttpClient],
      },
    }),

    SharedModule,
    OperationModule,
    DocumentationModule,

    ...MAT_MODULES,
  ],
  providers: [[]],
  bootstrap: [AppComponent],
})
export class AppModule {}

export function HttpLoaderFactory(http: HttpClient): TranslateHttpLoader {
  return new TranslateHttpLoader(http);
}
