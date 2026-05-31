import {
  ChangeDetectionStrategy,
  Component,
  Input,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { map } from 'rxjs/operators';
import { Entry } from '../../models';
import { DocumentationService } from '../../services';
import { Role } from '../../../shared/models';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-documentation-overview',
  templateUrl: './overview.component.html',
  styleUrls: ['./overview.component.scss'],
})
export class OverviewComponent implements OnInit, OnDestroy {
  @Input()
  title: string;

  entries$: Observable<Entry[]>;
  private destroy$ = new Subject<boolean>();

  constructor(private documentationService: DocumentationService) {}

  ngOnInit(): void {
    const documentationId = window.localStorage.getItem('documentationId');
    this.entries$ = this.documentationService
      .getDocumentation(documentationId)
      .pipe(map((documentation) => documentation.entries));
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  notSurgeon() {
    return window.sessionStorage.getItem('role') !== Role.SURGEON;
  }
}
