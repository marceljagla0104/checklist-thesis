import {Component, OnInit} from '@angular/core';
import {MatSnackBar} from '@angular/material/snack-bar';
import {take} from 'rxjs';
import {TranslateService} from '@ngx-translate/core';

// dropdown for timestamp selection
@Component({
  selector: 'app-settings-timestamp',
  templateUrl: './timestamp.component.html',
  styleUrls: ['./timestamp.component.scss'],
})
export class TimestampComponent implements OnInit {
  timestamps = ['END_TIME_ONLY', 'START_AND_END_TIME'];
  selectedTimestampSettings: string;
  currentTimestampSettings: string;

  constructor(
    private snackBar: MatSnackBar,
    private translate: TranslateService,
  ) {}

  changeTimestamp() {
    localStorage.setItem('timestamp', this.selectedTimestampSettings);
    this.showSuccessMessage();
  }

  private showSuccessMessage() {
    this.translate
      .get('SETTINGS_TIMESTAMP_SUCCESS')
      .pipe(take(1))
      .subscribe((res: string) => {
        this.snackBar.open(res, null, {
          duration: 2000,
          panelClass: 'green-snackbar',
        });
      });
  }

  ngOnInit(): void {
    this.currentTimestampSettings = localStorage.getItem('timestamp');
    this.selectedTimestampSettings = this.currentTimestampSettings;
  }
}
