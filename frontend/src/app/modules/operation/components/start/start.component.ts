import {ChangeDetectionStrategy, Component} from '@angular/core';

// checklist item for start events.... it's a simple component
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-start',
  templateUrl: './start.component.html',
  styleUrls: ['./start.component.scss'],
})
export class StartComponent {}
