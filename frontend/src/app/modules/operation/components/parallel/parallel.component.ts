import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {Child, Element} from '../../models';

// checklist item for parallel gateways. just starts new paths for the parallel paths
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-parallel',
  templateUrl: './parallel.component.html',
  styleUrls: ['./parallel.component.scss'],
})
export class ParallelComponent {
  @Input()
  children: Child[];

  @Input()
  paths: Map<string, Element[]>;

  getPathElements(id: string) {
    return this.paths.get(id);
  }
}
