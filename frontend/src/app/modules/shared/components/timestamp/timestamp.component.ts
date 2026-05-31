import { Component, EventEmitter, OnInit, Output } from '@angular/core';

@Component({
  selector: 'app-settings-timestamp',
  templateUrl: './timestamp.component.html',
  styleUrls: ['./timestamp.component.scss'],
})
export class TimestampComponent implements OnInit {
  @Output()
  timestamp = new EventEmitter<string>();

  timestamps = ['END_TIME_ONLY', 'START_AND_END_TIME'];
  selectedTimestampSettings: string;
  currentTimestampSettings: string;

  constructor() {}

  ngOnInit(): void {
    this.currentTimestampSettings = localStorage.getItem('timestamp');
    this.selectedTimestampSettings = this.currentTimestampSettings;
  }

  changeTimestamp() {
    this.timestamp.emit(this.selectedTimestampSettings);
  }
}
