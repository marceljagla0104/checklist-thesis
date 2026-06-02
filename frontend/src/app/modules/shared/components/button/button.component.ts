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

triggerControlAction() {
    console.log("Button geklickt - Sende Intent...");
    
    // Wir rufen die Funktion auf, die wir gerade im Service gebaut haben
    this.docService.createOrUpdateEntry(
      '123',              // documentationId (Wichtig: Diese ID muss in deiner DB existieren!)
      'test-element-01',  // elementId
      'Kamera Steuerung', // description
      undefined,          // startedAt
      undefined,          // finishedAt
      'ZOOM_CAMERA',      // textEvent
      'CONTROL_ACTION'    // <--- DAS IST DER WICHTIGE INTENT!
    ).subscribe({
      next: (res: any) => console.log('Erfolg:', res),
      error: (err: any) => console.error('Fehler:', err)
    });
  }
}


