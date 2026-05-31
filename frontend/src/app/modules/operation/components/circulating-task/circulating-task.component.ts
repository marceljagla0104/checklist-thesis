import {ChangeDetectionStrategy, Component, Input, OnDestroy, OnInit,} from '@angular/core';

import {Element} from '../../models';

import {BehaviorSubject, filter, Subject, takeUntil} from 'rxjs';
import {DocumentationService} from '../../../documentation/services';
import {SyncService} from '../../../shared/services';

// checklist item to display tasks done by the circulating nurse. appears orange when started and green when done
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-circulating-task',
  templateUrl: './circulating-task.component.html',
  styleUrls: ['./circulating-task.component.scss'],
})
export class CirculatingTaskComponent implements OnInit, OnDestroy {
  @Input()
  element: Element;

  finishedAt$: BehaviorSubject<Date> = new BehaviorSubject<Date>(null);

  private destroy$: Subject<boolean> = new Subject<boolean>();

  done$ = new BehaviorSubject<boolean>(false);
  started$ = new BehaviorSubject<boolean>(false);

  constructor(
    private documentationService: DocumentationService,
    private syncService: SyncService,
  ) {}

  ngOnDestroy(): void {
    this.finishedAt$.complete();
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  ngOnInit(): void {
    this.syncService
      .onCirculatingTaskStarted(this.element.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe((change) => {
        if (change.startedAt) {
          this.started$.next(true);
        }
      });

    this.syncService
      .onCirculatingTaskDone(this.element.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe((change) => {
        if (change.finishedAt) {
          this.finishedAt$.next(change.finishedAt);
          this.done$.next(true);
          this.started$.next(false);
        }
      });

    this.loadInitially();
  }

  private loadInitially() {
    this.documentationService
      .getEntry(window.localStorage.getItem('documentationId'), this.element.id)
      .pipe(filter((entry) => !!entry))
      .subscribe((entry) => {
        //todo set fields
        if (entry.startedAt) {
          this.started$.next(true);
        }

        if (entry.finishedAt) {
          this.finishedAt$.next(entry.finishedAt);
          this.started$.next(false);
          this.done$.next(true);
        }
      });
  }
}
