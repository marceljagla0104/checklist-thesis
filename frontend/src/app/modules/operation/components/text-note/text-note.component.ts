import {ChangeDetectionStrategy, Component, EventEmitter, Input, Output,} from '@angular/core';
import {ReplaySubject} from 'rxjs';
import {Role} from '../../../shared/models';

// checklist item for unforeseen events as text
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-text-note',
  templateUrl: './text-note.component.html',
  styleUrls: ['./text-note.component.scss'],
})
export class TextNoteComponent {
  @Input()
  set text(value: string) {
    if (!!value) {
      this.toggleDescription = true;
      this.isFilled = true;
    }
    this.text$.next(value);
  }

  @Input()
  disabled = false;

  @Output()
  description: EventEmitter<any> = new EventEmitter<any>();

  text$: ReplaySubject<string> = new ReplaySubject<string>(1);

  toggleDescription: boolean;

  isFilled = false;

  constructor() {
    const role = window.sessionStorage.getItem('role');
    if (role !== Role.SURGEON) {
      this.disabled = true;
    }
  }

  onEnterKeyPress($event: any) {
    const text = $event.target.value;

    if (text) {
      this.isFilled = true;
      this.description.emit({
        content: text,
        date: new Date(),
      });
    } else {
      this.isFilled = false;
    }
  }
}
