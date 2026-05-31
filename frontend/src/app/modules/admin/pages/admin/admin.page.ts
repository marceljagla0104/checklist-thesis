import {
  ChangeDetectionStrategy,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { AdminService } from '../../services';
import { BehaviorSubject, Subject } from 'rxjs';
import {
  DocumentationListItemDTO,
  OperationListItemDTO,
} from '../../services/api/admin';
import { ToastService } from '../../../shared/services';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './admin.page.html',
  styleUrls: ['./admin.page.scss'],
})
export class AdminPage implements OnInit, OnDestroy {
  selectedFile: File | null = null;

  operations$: Subject<OperationListItemDTO[]> = new Subject();

  finished$: BehaviorSubject<DocumentationListItemDTO[]> = new BehaviorSubject<
    DocumentationListItemDTO[]
  >([]);

  unfinished$: BehaviorSubject<DocumentationListItemDTO[]> =
    new BehaviorSubject<DocumentationListItemDTO[]>([]);

  constructor(
    private adminService: AdminService,
    private toastService: ToastService,
  ) {}

  ngOnInit(): void {
    this.adminService.listDocumentations().subscribe((docs) => {
      this.finished$.next(docs.filter((doc) => doc.savedAt));
      this.unfinished$.next(docs.filter((doc) => !doc.savedAt));
    });

    this.loadOperationList();
  }

  ngOnDestroy(): void {
    this.finished$.complete();
    this.unfinished$.complete();
    this.operations$.complete();
  }

  loadOperationList() {
    this.adminService.listOperations().subscribe((operations) => {
      this.operations$.next([...operations]);
    });
  }

  onFileSelected(event: any) {
    this.selectedFile = event.target.files[0];
  }

  onSubmit() {
    if (this.selectedFile) {
      this.adminService.uploadOperation(this.selectedFile).subscribe(
        (_) => this.loadOperationList(),
        (error) =>
          this.toastService.showToast({
            text: error.message,
            duration: 3000,
            color: 'warn',
          }),
      );
    }
  }

  deleteOperation(id: string) {
    this.adminService.deleteOperation(id).subscribe((_) => {
      this.loadOperationList();
    });
  }
}
