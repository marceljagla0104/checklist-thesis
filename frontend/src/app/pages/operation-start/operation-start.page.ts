import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, Observable, of } from 'rxjs';
import { OperationListItemDTO } from '../../modules/operation/services/api/operation';
import { OperationService } from '../../modules/operation/services';
import { DocumentationListItemDTO } from '../../modules/documentation/services/api/documentation';
import { DocumentationService } from '../../modules/documentation/services';
import { ToastService } from '../../modules/shared/services';
import { Role } from '../../modules/shared/models';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './operation-start.page.html',
  styleUrls: ['./operation-start.page.scss'],
})
export class OperationStartPage implements OnInit {
  step = 1;

  operations$: Observable<OperationListItemDTO[]>;
  documentations$: Observable<DocumentationListItemDTO[]>;

  opId: string;

  role = window.sessionStorage.getItem('role');

  constructor(
    private router: Router,
    private operationService: OperationService,
    private documentationService: DocumentationService,
    private toastService: ToastService,
  ) {}

  nextStep() {
    this.step++;
  }

  back() {
    if (this.step === 1) {
      this.router.navigate(['..']);
      return;
    }
    this.step--;
  }

  operation(operationId: string) {
    this.opId = operationId;
    this.documentations$ =
      this.documentationService.listDocumentationsByOperationId(this.opId);

    this.nextStep();
  }

  ngOnInit(): void {
    this.operations$ = this.operationService.listOperations().pipe(
      catchError((err) => {
        this.toastService.showToast({
          text: 'Error loading operations',
          duration: 2000,
          color: 'warn',
        });
        return of([]);
      }),
    );
  }

  createDocumentation() {
    this.documentationService
      .createDocumentation(this.opId)
      .subscribe((docuId) => {
        window.localStorage.setItem('documentationId', docuId);
        this.router.navigate(['checklist', this.opId]);
      });
  }

  existingDocumentation(id: string) {
    window.localStorage.setItem('documentationId', id);
    this.router.navigate(['checklist', this.opId]);
  }

  protected readonly Role = Role;
}
