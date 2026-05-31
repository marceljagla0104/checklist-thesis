import { Component, EventEmitter, Output } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-settings-language',
  templateUrl: './language.component.html',
  styleUrls: ['./language.component.scss'],
})
export class LanguageComponent {
  @Output()
  language = new EventEmitter<string>();

  languages = ['de', 'en'];
  selectedLanguage: string;
  currentLanguage: string;

  constructor(private translate: TranslateService) {
    this.currentLanguage = this.translate.currentLang;
    this.selectedLanguage = this.currentLanguage;
  }

  changeLanguage() {
    this.language.emit(this.selectedLanguage);
  }
}
