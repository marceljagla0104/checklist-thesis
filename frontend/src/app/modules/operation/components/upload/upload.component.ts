import {ChangeDetectionStrategy, Component, Input, OnDestroy, OnInit,} from '@angular/core';
import {Role} from '../../../shared/models';
import {DocumentationService} from '../../../documentation/services';
import {SyncService} from '../../../shared/services';
import {BehaviorSubject, filter, Subject, takeUntil} from 'rxjs';

// checklist item to upload txt files
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-upload',
  templateUrl: './upload.component.html',
  styleUrls: ['./upload.component.scss'],
})
export class UploadComponent implements OnInit, OnDestroy {
  @Input()
  id: string;

  @Input()
  name: string;

  disabled: boolean = false;

  fileContent$ = new BehaviorSubject<string>('');

  destroy$ = new Subject<boolean>();

  constructor(
    private documentationService: DocumentationService,
    private syncService: SyncService,
  ) {}

  ngOnInit(): void {
    this.disabled = window.sessionStorage.getItem('role') !== Role.SURGEON;
    this.loadStatusInitially();
    this.listenToStatusChanges();
  }

  ngOnDestroy(): void {
    this.fileContent$.complete();
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  onFileSelected($event: Event) {
    const file = ($event.target as HTMLInputElement).files[0];
    const reader = new FileReader();

    reader.onload = () => {
      const content = reader.result as string;
      this.fileContent$.next(content);
      this.documentationService
        .createOrUpdateEntry(
          window.localStorage.getItem('documentationId'),
          this.id,
          content,
          null,
          new Date(),
        )
        .subscribe();
    };

    reader.readAsText(file);
  }

  private loadStatusInitially() {
    this.documentationService
      .loadEntry(this.id)
      .pipe(filter((entry) => !!entry))
      .subscribe((entry) => {
        this.fileContent$.next(entry.description);
      });
  }

  private listenToStatusChanges() {
    this.syncService
      .onEntryUpdated(this.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe((change) => {
        if (change.finishedAt) {
          this.fileContent$.next(change.description);
        }
      });

    this.syncService
      .onEntryRemoved(this.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe((change) => {
        this.fileContent$.next('');
      });
  }
}
