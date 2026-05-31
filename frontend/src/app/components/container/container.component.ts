import { Component } from '@angular/core';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { SettingsDialog } from '../../modules/shared/dialogs';
import { take } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';
import { ToastService } from '../../modules/shared/services';

@Component({
  selector: 'app-container',
  templateUrl: './container.component.html',
  styleUrls: ['./container.component.scss'],
})
export class ContainerComponent {
  constructor(
    private dialog: MatDialog,
    private translate: TranslateService,
    private toastService: ToastService,
  ) {}

  openSettings() {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.hasBackdrop = true;

    const dialogRef = this.dialog.open(SettingsDialog, dialogConfig);

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.showSuccessMessage();
      }
    });
  }

  private showSuccessMessage() {
    this.translate
      .get('SETTINGS_SUCCESS')
      .pipe(take(1))
      .subscribe((res: string) => {
        this.toastService.showToast({
          text: res,
          duration: 2000,
        });
      });
  }
}
