import { Injectable } from '@angular/core';
import { Observable, ReplaySubject } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';

export enum Setting {
  LANGUAGE = 'lang',
  TIMESTAMP = 'timestamp',
  ERROR_SOUND = 'errorSoundEnabled',    //Key im Enum für den error sound gesetzt
}

@Injectable({
  providedIn: 'root',
})
export class SettingsService {
  timestamp$ = new ReplaySubject<string>(1);
  language$ = new ReplaySubject<string>(1);
  errorSoundEnabled$ = new ReplaySubject<string>(1);

  constructor(private translate: TranslateService) {
    this.timestamp$.next(localStorage.getItem(Setting.TIMESTAMP));
    this.language$.next(localStorage.getItem(Setting.LANGUAGE));
    const savedSound = localStorage.getItem(Setting.ERROR_SOUND);
    this.errorSoundEnabled$.next(savedSound != null ? savedSound : 'true');

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
        case Setting.ERROR_SOUND:
          localStorage.setItem(Setting.ERROR_SOUND, value);
          this.errorSoundEnabled$.next(value);
          break;
      }
    });
  }

  getTimestampSettings(): Observable<string> {
    return this.timestamp$;
  }

  getErrorSoundSetting(): Observable<string>  {
    return this.errorSoundEnabled$;
  }
}
