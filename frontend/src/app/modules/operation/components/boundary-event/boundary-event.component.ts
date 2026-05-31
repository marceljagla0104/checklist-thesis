import {ChangeDetectionStrategy, Component, Input, OnDestroy, OnInit,} from '@angular/core';
import {BehaviorSubject, filter, Subject, takeUntil} from 'rxjs';
import {Element} from '../../models';
import {DocumentationService} from '../../../documentation/services';
import {SyncService} from '../../../shared/services';

// checklist element that displays boundary events as box  with checkbox
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-boundary-event',
  templateUrl: './boundary-event.component.html',
  styleUrls: ['./boundary-event.component.scss'],
})
export class BoundaryEventComponent implements OnInit, OnDestroy {
  @Input()
  id: string;

  @Input()
  name: string;

  @Input()
  elements: Element[];

  @Input()
  disabled: boolean = false;

  checked$ = new BehaviorSubject<boolean>(false);

  documentationId = window.localStorage.getItem('documentationId');

  destroy$: Subject<boolean> = new Subject<boolean>();

  constructor(
    private documentationService: DocumentationService,
    private syncService: SyncService,
  ) {}

  ngOnInit(): void {
    this.loadStatusInitially();
    this.listenToStatusChanges();
  }

  private loadStatusInitially() {
    this.documentationService
      .loadEntry(this.id)
      .pipe(filter((entry) => !!entry))
      .subscribe((entry) => {
        if (entry.startedAt || entry.finishedAt) {
          this.checked$.next(true);
        }
      });
  }

  onChange() {
    this.checked$.next(!this.checked$.getValue());
    if (this.checked$.getValue()) {
      this.documentationService
        .createOrUpdateEntry(
          this.documentationId,
          this.id,
          this.name,
          null,
          new Date(),
        )
        .subscribe();
    } else {
      this.documentationService
        .removeEntry(this.documentationId, this.id)
        .subscribe();
    }
  }

  private listenToStatusChanges() {
    this.syncService
      .onEntryUpdated(this.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe((change) => {
        if (change.finishedAt) {
          this.checked$.next(true);
        }
      });

    this.syncService
      .onEntryRemoved(this.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.checked$.next(false);
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}
