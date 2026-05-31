import { Component } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent {
  title = 'Checklist';

  constructor(private translate: TranslateService) {
    const browserLang = navigator.language;
    const savedLang = localStorage.getItem('lang');
    const lang = savedLang ? savedLang : browserLang;

    this.translate.setDefaultLang('en');
    this.translate.use(lang);
  }
}
