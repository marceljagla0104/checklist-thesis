import { Component, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { SettingsService } from '../../services';

@Component({
  templateUrl: './settings.dialog.html',
  styleUrls: ['./settings.dialog.scss'],
})
export class SettingsDialog implements OnInit {
  private settingsMap: Map<string, string>;

  role: string;

  constructor(
    private dialogRef: MatDialogRef<SettingsDialog>,
    private settingsService: SettingsService,
  ) {}

  ngOnInit(): void {
    this.settingsMap = new Map<string, string>();
    this.role = window.sessionStorage.getItem('role');
  }

  onSave() {
    this.settingsService.setItems(this.settingsMap);
    this.dialogRef.close(true);
  }

  onCancel() {
    this.dialogRef.close(false);
  }

  addLanguage($event: string) {
    this.settingsMap.set('lang', $event);
  }

  addTimestamp($event: string) {
    this.settingsMap.set('timestamp', $event);
  }
}
