import { Injectable } from '@angular/core';
import { filter, ReplaySubject } from 'rxjs';

export interface Toast {
  text: string;
  duration: number;
  color?: 'warn' | 'default';
}

// service to display toasts in the application
@Injectable({
  providedIn: 'root',
})
export class ToastService {
  private toast$ = new ReplaySubject<Toast>(1);

  constructor() {}

  toasts() {
    return this.toast$.asObservable().pipe(filter((n) => !!n));
  }

  showToast(toast: Toast) {
    this.toast$.next(toast);
  }

  clear() {
    this.toast$.next(null);
  }
}
