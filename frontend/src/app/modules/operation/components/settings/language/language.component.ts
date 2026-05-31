import {Component} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {MatSnackBar} from '@angular/material/snack-bar';
import {take} from 'rxjs';

// dropdown for language selection
@Component({
  selector: 'app-settings-language',
  templateUrl: './language.component.html',
  styleUrls: ['./language.component.scss'],
})
export class LanguageComponent {
  languages = ['de', 'en'];
  selectedLanguage: string;
  currentLanguage: string;

  constructor(
    private translate: TranslateService,
    private snackBar: MatSnackBar,
  ) {
    this.currentLanguage = translate.currentLang;
    this.selectedLanguage = this.currentLanguage;
  }

  changeLanguage() {
    this.translate.use(this.selectedLanguage);
    localStorage.setItem('lang', this.selectedLanguage);
    this.showSuccessMessage();
  }

  private showSuccessMessage() {
    this.translate
      .get('SETTINGS_LANGUAGE_SUCCESS')
      .pipe(take(1))
      .subscribe((res: string) => {
        this.snackBar.open(res, null, {
          duration: 2000,
          panelClass: 'green-snackbar',
        });
      });
  }
}
