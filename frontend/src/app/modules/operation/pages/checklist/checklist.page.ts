import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  OnDestroy,
  OnInit,
  ViewChild,
} from '@angular/core';
import { OperationService } from '../../services';
import { Operation } from '../../models';
import { ActivatedRoute } from '@angular/router';
import { BehaviorSubject, Subject, takeUntil } from 'rxjs';
import { Role } from '../../../shared/models';
import { SyncService } from '../../../shared/services';
import { DocumentationService } from '../../../documentation/services';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './checklist.page.html',
  styleUrls: ['./checklist.page.scss'],
})
export class ChecklistPage implements OnInit, OnDestroy {
  operation$: Subject<Operation> = new Subject<Operation>();

  role = window.sessionStorage.getItem('role');

  selectedTab$: BehaviorSubject<string> = new BehaviorSubject<string>('');

  destroy$: Subject<boolean> = new Subject<boolean>();

  @ViewChild('content') content: ElementRef;

  constructor(
    private operationService: OperationService,
    private documentationService: DocumentationService,
    private route: ActivatedRoute,
    private syncService: SyncService,
  ) {}

  ngOnDestroy(): void {
    this.operation$.complete();
  }

  ngOnInit(): void {
    this.syncService.connectWebSocket();
    this.route.params.subscribe((params) => {
      const id = params['id'];
      this.operationService.getOperation(id).subscribe((operation) => {
        console.log('operation', operation);
        this.operation$.next(operation);
        this.selectedTab$.next(operation.subprocesses[0].name);
      });
    });

    if (this.role !== Role.SURGEON) {
      this.syncService
        .onTabChange()
        .pipe(takeUntil(this.destroy$))
        .subscribe((tabName) => {
          this.selectedTab$.next(tabName);
          this.content.nativeElement.scrollTo(0, 0);
        });
    }

    this.documentationService
      .getDocumentation(window.localStorage.getItem('documentationId'))
      .subscribe((documentation) => {
        this.documentationService.cacheDocumentation(documentation);
      });
  }

  selectTab(tabName: string) {
    this.selectedTab$.next(tabName);
    this.content.nativeElement.scrollTo(0, 0);
    if (tabName !== 'documentation' && this.role === Role.SURGEON) {
      this.syncService.changeTab(tabName).subscribe();
    }
  }

  hasSurgeonRole() {
    return this.role === Role.SURGEON || this.role === Role.STUDENT;
  }
}
