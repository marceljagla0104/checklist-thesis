import {ChangeDetectionStrategy, Component, OnDestroy, OnInit,} from '@angular/core';
import {SyncService} from '../../../shared/services';
import {TranslateService} from '@ngx-translate/core';
import {BehaviorSubject, filter, Subject, switchMap, takeUntil} from 'rxjs';
import {DocumentationService} from '../../../documentation/services';

// button to call circulating nurse. appears orange when started and blue when done
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-call-circulating',
  templateUrl: './call-circulating.component.html',
  styleUrls: ['./call-circulating.component.scss'],
})
export class CallCirculatingComponent implements OnInit, OnDestroy {
  destroy$: Subject<boolean> = new Subject<boolean>();
  done$ = new BehaviorSubject<boolean>(false);
  started$ = new BehaviorSubject<boolean>(false);

  constructor(
    private communicationService: SyncService,
    private syncService: SyncService,
    private translate: TranslateService,
    private documentationService: DocumentationService,
  ) {}

  callCirculatingNurse() {
    this.translate
      .get('CALL_CIRCULATING')
      .pipe(
        switchMap((desc) =>
          this.communicationService.callCirculatingNurse(desc),
        ),
      )
      .subscribe();
  }

  ngOnInit(): void {
    this.syncService
      .onCirculatingTaskStarted('call-circulating')
      .pipe(takeUntil(this.destroy$))
      .subscribe((change) => {
        if (change.startedAt) {
          this.done$.next(false);
          this.started$.next(true);
        }
      });

    this.syncService
      .onCirculatingTaskDone('call-circulating')
      .pipe(takeUntil(this.destroy$))
      .subscribe((change) => {
        if (change.finishedAt) {
          this.done$.next(true);
          this.started$.next(false);
        }
      });

    this.loadInitially();
  }

  private loadInitially() {
    console.log('loadInitially');
    this.documentationService
      .getEntry(
        window.localStorage.getItem('documentationId'),
        'call-circulating',
      )
      .pipe(filter((entry) => !!entry))
      .subscribe((entry) => {
        if (entry.finishedAt) {
          this.started$.next(false);
          this.done$.next(true);
        }

        if (entry.startedAt && !entry.finishedAt) {
          this.done$.next(false);
          this.started$.next(true);
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
    this.done$.complete();
    this.started$.complete();
  }
}
