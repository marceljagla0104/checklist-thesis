import { ChangeDetectionStrategy, Component } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { WebcamImage } from 'ngx-webcam';
import { Role } from 'src/app/modules/shared/models';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './camera.dialog.html',
  styleUrls: ['./camera.dialog.scss'],
})
export class CameraDialog {
  private trigger$: Subject<any> = new Subject();
  public webcamImage!: WebcamImage;
  private nextWebcam$: Subject<any> = new Subject();
  sysImage = '';

  disabled = false;

  constructor(private dialogRef: MatDialogRef<CameraDialog>) {
    const role = window.sessionStorage.getItem('role');
    if (role !== Role.SURGEON) {
      this.disabled = true;
    }
  }

  public getSnapshot(): void {
    this.trigger$.next(void 0);
  }

  public captureImg(webcamImage: WebcamImage): void {
    this.webcamImage = webcamImage;
    this.sysImage = webcamImage!.imageAsDataUrl;
  }

  public get invokeObservable(): Observable<any> {
    return this.trigger$.asObservable();
  }

  public get nextWebcamObservable(): Observable<any> {
    return this.nextWebcam$.asObservable();
  }

  onSave() {
    this.dialogRef.close(this.webcamImage?.imageAsDataUrl);
  }

  onCancel() {
    this.dialogRef.close();
  }

  discard() {
    this.webcamImage = undefined;
    this.sysImage = undefined;
  }
}
