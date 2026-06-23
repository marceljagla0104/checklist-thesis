import { Component, EventEmitter, Input, Output } from '@angular/core';
import { DocumentationService } from 'src/app/modules/documentation/services';

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

  constructor(private docService:DocumentationService){}

  onClick(_: Event) {
    this.clickCallback.emit();
  }
}


