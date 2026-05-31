import {ChangeDetectionStrategy, Component, Input, OnInit,} from '@angular/core';
import {Child, Element, InputType, Type} from '../../models';
import {Role} from '../../../shared/models';

// main component to render all checklist items of one path. is used in other components too
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-path',
  templateUrl: './path.component.html',
  styleUrls: ['./path.component.scss'],
})
export class PathComponent implements OnInit {
  @Input()
  elements: Element[];

  @Input()
  path: string = '';

  startElement: Element;

  pathElements: Element[] = [];

  hiddenElements: string[];

  activatedXOR: string[] = [];

  XOR = Type.XOR;
  PARALLEL = Type.PARALLEL;
  TASK = Type.TASK;
  START_EVENT = Type.START_EVENT;
  END_EVENT = Type.END_EVENT;

  CHECKBOX = InputType.CHECKBOX;
  TEXT = InputType.TEXT;
  UPLOAD = InputType.UPLOAD;
  PICTURE = InputType.PICTURE;
  HEADING = InputType.HEADING;

  toToggle: Map<string, boolean> = new Map<string, boolean>();
  CIRCULATING = Role.CIRCULATING;

  ngOnInit(): void {
    this.pathElements = this.elements.filter(
      (element) =>
        element.pathIds[element.pathIds.length - 1] === this.path ||
        (element.pathIds.length === 0 && this.path === ''),
    );
    this.startElement = this.elements.find(
      (element) => element.type === Type.START_EVENT,
    );
    this.hideElementsAfterXOR();
  }

  private hideElementsAfterXOR() {
    let reachedXOR = false;
    this.hiddenElements = [];
    this.pathElements.forEach((element) => {
      if (reachedXOR) {
        this.hiddenElements.push(element.id);
      }

      const xorNotActivated = !this.activatedXOR.includes(element.id);
      const isStartXor = element.children.length > 1;
      if (element.type === Type.XOR && xorNotActivated && isStartXor) {
        reachedXOR = true;
      }
    });
  }

  getEvents(eventIds: string[]): Element[] {
    return this.elements.filter((element) => eventIds.includes(element.id));
  }

  getPathsForChildren(children: Child[]): Map<string, Element[]> {
    const pathIds = children.map((child) => child.id);
    return this.getPaths(pathIds);
  }

  getPaths(pathIds: string[]): Map<string, Element[]> {
    const map = new Map<string, Element[]>();
    pathIds.forEach((id) => {
      map.set(
        id,
        this.elements.filter((element) => element.pathIds.includes(id)),
      );
    });

    return map;
  }

  isHidden(element: Element) {
    return (
      this.hiddenElements.includes(element.id) || !this.isCurrentPath(element)
    );
  }

  isCurrentPath(element: Element) {
    return (
      element.pathIds[element.pathIds.length - 1] === this.path ||
      (element.pathIds.length === 0 && this.path === '')
    );
  }

  // decides what elements to show depending on activated XORs
  showElements(xorId: string) {
    this.activatedXOR.push(xorId);
    this.hideElementsAfterXOR();
  }
}
