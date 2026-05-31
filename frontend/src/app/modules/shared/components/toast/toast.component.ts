import {Component, OnDestroy, OnInit} from '@angular/core';
import {Toast, ToastService} from '../../services';
import {BehaviorSubject, Subject, takeUntil} from 'rxjs';
import {animate, style, transition, trigger} from '@angular/animations';

@Component({
  selector: 'app-toast',
  templateUrl: './toast.component.html',
  styleUrls: ['./toast.component.scss'],
  animations: [
    trigger('slideInOut', [
      transition(':enter', [
        style({transform: 'translateY(100%)'}),
        animate('0.2s ease-out', style({transform: 'translateY(0)'})),
      ]),
      transition(':leave', [
        style({transform: 'translateY(0)'}),
        animate('0.2s ease-in', style({transform: 'translateY(100%)'})),
      ]),
    ]),
  ],
})
export class ToastComponent implements OnInit, OnDestroy {
  destroy$: Subject<boolean> = new Subject();
  toast$ = new BehaviorSubject<Toast>(null);

  constructor(private toastService: ToastService) {
  }

  ngOnDestroy(): void {
    this.toast$.complete();
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  ngOnInit(): void {
    this.toastService
      .toasts()
      .pipe(takeUntil(this.destroy$))
      .subscribe((toast) => {
        this.toast$.next(toast);
        setTimeout(() => {
          this.toast$.next(null);
          this.toastService.clear();
        }, toast.duration);
      });
  }
}
