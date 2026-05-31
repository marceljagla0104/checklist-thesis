import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {Element} from '../../models';

// renders the root path of the subprocess
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-subprocess',
  templateUrl: './subprocess.component.html',
  styleUrls: ['./subprocess.component.scss'],
})
export class SubprocessComponent {
  @Input()
  elements: Element[];
}
