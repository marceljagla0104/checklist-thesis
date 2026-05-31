import {ChangeDetectionStrategy, Component, Input} from '@angular/core';

// tab component to display different subprocesses
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-subprocess-tab',
  templateUrl: './subprocess-tab.component.html',
  styleUrls: ['./subprocess-tab.component.scss'],
})
export class SubprocessTabComponent {
  @Input()
  name: string;
}
