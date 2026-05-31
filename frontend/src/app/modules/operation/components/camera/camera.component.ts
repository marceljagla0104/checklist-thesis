import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {BehaviorSubject} from 'rxjs';
import {Role} from 'src/app/modules/shared/models';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {CameraDialog} from '../../dialogs';
import {ImageCacheService} from '../../../shared/services/image-cache.service';
import {DocumentationService} from '../../../documentation/services';

// checklist item to let you take a picture
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-camera',
  templateUrl: './camera.component.html',
  styleUrls: ['./camera.component.scss'],
})
export class CameraComponent {
  @Input()
  elementId: string;

  image$ = new BehaviorSubject('');

  disabled = false;

  constructor(
    private dialog: MatDialog,
    private imageCache: ImageCacheService,
    private documentationService: DocumentationService,
  ) {
    const role = window.sessionStorage.getItem('role');
    if (role !== Role.SURGEON) {
      this.disabled = true;
    }
  }

  toggleCamera() {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.hasBackdrop = true;

    const dialogRef = this.dialog.open(CameraDialog, dialogConfig);

    dialogRef.afterClosed().subscribe((img) => {
      if (img) {
        this.image$.next(img);
        this.imageCache.saveImage(this.elementId, img); // save image to cache to use in documentation tab

        this.documentationService
          .createOrUpdateEntry(
            window.localStorage.getItem('documentationId'),
            this.elementId,
            '',
            null,
            new Date(),
          )
          .subscribe();
      }
    });
  }
}
