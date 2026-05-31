import {ChangeDetectionStrategy, Component, Input, OnDestroy, OnInit,} from '@angular/core';
import {BehaviorSubject, filter, Subject, takeUntil} from 'rxjs';
import {Role} from '../../../shared/models';
import {DocumentationService} from '../../../documentation/services';
import {SyncService} from '../../../shared/services';

// checklist item for text inputs
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-text',
  templateUrl: './text.component.html',
  styleUrls: ['./text.component.scss'],
})
export class TextComponent implements OnInit, OnDestroy {
  @Input()
  id: string;

  @Input()
  name: string;

  text$ = new BehaviorSubject<string>('');
  disabled = false;

  documentationId: string;

  isFilled = false;

  private DESCRIPTION_NAME: string;

  private destroy$: Subject<boolean> = new Subject<boolean>();

  constructor(
    private documentationService: DocumentationService,
    private syncService: SyncService,
  ) {
    const role = window.sessionStorage.getItem('role');
    if (role !== Role.SURGEON) {
      this.disabled = true;
    }
  }

  ngOnDestroy(): void {
    this.text$.complete();
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  ngOnInit(): void {
    this.DESCRIPTION_NAME = this.name + ': ';
    this.documentationId = window.localStorage.getItem('documentationId');
    this.loadStatusInitially();
    this.listenToStatusChanges();
  }

  onEnterKeyPress(event: any) {
    this.text$.next(event.target.value);
    if (!this.text$.value) {
      this.isFilled = false;
      this.documentationService
        .removeEntry(this.documentationId, this.id)
        .subscribe();
      return;
    }

    this.isFilled = true;

    this.documentationService
      .createOrUpdateEntry(
        this.documentationId,
        this.id,
        this.DESCRIPTION_NAME + this.text$.value,
        null,
        new Date(),
      )
      .subscribe();
  }

  private loadStatusInitially() {
    this.documentationService
      .loadEntry(this.id)
      .pipe(filter((entry) => !!entry))
      .subscribe((entry) => {
        this.text$.next(this.removeNameFromDescription(entry.description));
        this.isFilled =
          this.removeNameFromDescription(entry.description).length > 0;
      });
  }

  private removeNameFromDescription(description: string): string {
    return description.replace(this.DESCRIPTION_NAME, '');
  }

  private listenToStatusChanges() {
    this.syncService
      .onEntryUpdated(this.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe((change) => {
        if (change.finishedAt) {
          this.text$.next(this.removeNameFromDescription(change.description));
        }
      });

    this.syncService
      .onEntryRemoved(this.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe((change) => {
        this.text$.next('');
      });
  }
}
