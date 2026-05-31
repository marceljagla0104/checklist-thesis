import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-button',
  templateUrl: './button.component.html',
  styleUrls: ['./button.component.scss'],
})
export class ButtonComponent {
  @Input()
  disabled: boolean = false;

  @Output()
  clickCallback: EventEmitter<any> = new EventEmitter<any>();

  onClick(_: Event) {
    this.clickCallback.emit();
  }
}
