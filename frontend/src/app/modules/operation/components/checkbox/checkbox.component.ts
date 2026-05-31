import {ChangeDetectionStrategy, Component, Input, OnDestroy, OnInit,} from '@angular/core';

import {Element} from '../../models';

import {BehaviorSubject, filter, Observable, Subject, takeUntil} from 'rxjs';
import {Role} from '../../../shared/models';
import {SettingsService, SyncService} from '../../../shared/services';
import {map} from 'rxjs/operators';
import {DocumentationService} from '../../../documentation/services';

// checklist item to display checkboxes
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-checkbox',
  templateUrl: './checkbox.component.html',
  styleUrls: ['./checkbox.component.scss'],
})
export class CheckboxComponent implements OnInit, OnDestroy {
  @Input()
  element: Element;

  @Input()
  paths: Map<string, Element[]>;

  @Input()
  events: Element[];

  endIsChecked$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

  startIsChecked$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(
    false,
  );

  finishedAt$: BehaviorSubject<Date> = new BehaviorSubject<Date>(null);

  startedAt$: BehaviorSubject<Date> = new BehaviorSubject<Date>(null);

  isSkipped$: Observable<boolean>;

  textEvent$: BehaviorSubject<string> = new BehaviorSubject<string>('');

  showInfo: boolean = false;

  disabled: boolean;

  info: string;

  instruments: string[];

  startAndEndTime$: Observable<boolean>;

  documentationId: string;
  private destroy$: Subject<boolean> = new Subject<boolean>();

  constructor(
    private documentationService: DocumentationService,
    private syncService: SyncService,
    private settings: SettingsService,
  ) {}

  ngOnDestroy(): void {
    this.startedAt$.complete();
    this.finishedAt$.complete();
    this.startIsChecked$.complete();
    this.endIsChecked$.complete();
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  ngOnInit(): void {
    this.isSkipped$ = this.documentationService
      .hasAnyEntry(this.element.allChildIds)
      .pipe(takeUntil(this.destroy$));

    this.startAndEndTime$ = this.settings.getTimestampSettings().pipe(
      takeUntil(this.destroy$),
      map((s) => s === 'START_AND_END_TIME'),
    );
    this.disableDependingOnRole();

    this.documentationId = window.localStorage.getItem('documentationId');
    this.loadCheckboxStatusInitially();
    this.listenToCheckboxStatusChanges();
  }

  private disableDependingOnRole() {
    let role = sessionStorage.getItem('role') as Role;
    this.disabled = !(role === Role.SURGEON);
    if (role === Role.SURGEON) {
      this.info = this.element.surgeonInfo;
    }

    if (role === Role.STUDENT) {
      this.info = this.element.studentInfo;
      this.toggleInfo();
    }

    if (role === Role.SCRUB) {
      this.instruments = this.element.instruments;
    }
  }

  private listenToCheckboxStatusChanges() {
    this.syncService
      .onEntryUpdated(this.element.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe((change) => {
        if (change.startedAt) {
          this.startIsChecked$.next(true);
          this.startedAt$.next(change.startedAt);
        } else {
          this.startedAt$.next(null);
          this.startIsChecked$.next(false);
        }

        if (change.finishedAt) {
          this.finishedAt$.next(change.finishedAt);
          this.endIsChecked$.next(true);
        } else {
          this.finishedAt$.next(null);
          this.endIsChecked$.next(false);
        }

        if (change.textEvent) {
          this.textEvent$.next(change.textEvent);
        }
      });

    this.syncService
      .onEntryRemoved(this.element.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.startIsChecked$.next(false);
        this.startedAt$.next(null);
        this.finishedAt$.next(null);
        this.endIsChecked$.next(false);
      });

    this.syncService
      .onCirculatingTaskDone(this.element.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe((change) => {
        if (change.finishedAt) {
          this.finishedAt$.next(change.finishedAt);
          this.endIsChecked$.next(true);
        }
      });
  }

  private loadCheckboxStatusInitially() {
    this.documentationService
      .loadEntry(this.element.id)
      .pipe(filter((entry) => !!entry))
      .subscribe((entry) => {
        if (entry.startedAt) {
          this.startedAt$.next(entry.startedAt);
          this.startIsChecked$.next(true);
        }

        if (entry.finishedAt) {
          this.finishedAt$.next(entry.finishedAt);
          this.endIsChecked$.next(true);
        }

        if (entry.textEvent) {
          this.textEvent$.next(entry.textEvent);
        }
      });
  }

  onStartChange() {
    this.startIsChecked$.next(!this.startIsChecked$.value);
    if (this.startIsChecked$.value) {
      if (this.endIsChecked$.value) {
        this.endIsChecked$.next(false);
        this.onEndChange();
      }

      this.documentationService
        .createOrUpdateEntry(
          this.documentationId,
          this.element.id,
          this.element.name,
          new Date(),
          null,
        )
        .subscribe();
    }

    if (!this.startIsChecked$.value) {
      this.documentationService
        .removeEntry(this.documentationId, this.element.id)
        .subscribe();
      this.startedAt$.next(null);
      this.finishedAt$.next(null);
      this.endIsChecked$.next(false);
    }
  }

  onEndChange() {
    this.endIsChecked$.next(!this.endIsChecked$.value);
    if (this.endIsChecked$.value) {
      const finished = new Date();
      this.finishedAt$.next(finished);

      this.documentationService
        .createOrUpdateEntry(
          this.documentationId,
          this.element.id,
          this.element.name,
          this.startedAt$.value,
          finished,
        )
        .subscribe();

      if (this.element.circulatingTriggerId) {
        this.documentationService
          .callCirculating(this.element.circulatingTriggerId)
          .subscribe();
      }
    } else {
      if (this.startedAt$.value) {
        this.finishedAt$.next(null);
        this.documentationService
          .createOrUpdateEntry(
            this.documentationId,
            this.element.id,
            this.element.name,
            this.startedAt$.value,
            this.finishedAt$.value,
          )
          .subscribe();
      } else {
        this.documentationService
          .removeEntry(this.documentationId, this.element.id)
          .subscribe();
      }
      this.finishedAt$.next(null);
    }
  }

  toggleInfo() {
    this.showInfo = !this.showInfo;
  }

  addTextDescription($event: any) {
    this.documentationService
      .createOrUpdateEntry(
        this.documentationId,
        this.element.id,
        this.element.name,
        this.startedAt$.value,
        this.finishedAt$.value,
        $event.content,
      )
      .subscribe();
  }
}
