import { Injectable } from '@angular/core';
import { Observable, ReplaySubject } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';

export enum Setting {
  LANGUAGE = 'lang',
  TIMESTAMP = 'timestamp',
}

@Injectable({
  providedIn: 'root',
})
export class SettingsService {
  timestamp$ = new ReplaySubject<string>(1);
  language$ = new ReplaySubject<string>(1);

  constructor(private translate: TranslateService) {
    this.timestamp$.next(localStorage.getItem(Setting.TIMESTAMP));
    this.language$.next(localStorage.getItem(Setting.LANGUAGE));
  }

  setItems(items: Map<string, string>) {
    items.forEach((value, key) => {
      switch (key) {
        case Setting.LANGUAGE:
          localStorage.setItem(Setting.LANGUAGE, value);
          this.language$.next(value);
          this.translate.use(value);
          break;
        case Setting.TIMESTAMP:
          localStorage.setItem(Setting.TIMESTAMP, value);
          this.timestamp$.next(value);
          break;
      }
    });
  }

  getTimestampSettings(): Observable<string> {
    return this.timestamp$;
  }
}
