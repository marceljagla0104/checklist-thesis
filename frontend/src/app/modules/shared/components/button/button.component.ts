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
    const successSound = new Audio('assets/sounds/success.mp3');
    const errorSound = new Audio('assets/sounds/error.mp3');
    const activeDocId = this.docService.getActiveDocumentationId();

    if (!activeDocId){
      console.error("Fehler: Keine aktive Dokumentation im Service gefunden!");
      return;
    }
    
    console.log("Button geklickt - Sende Intent für Doku-ID", activeDocId);

    this.docService.createOrUpdateEntry(
      activeDocId,              // documentationId 
      'test-element-01',  // elementId
      'Kamera zoomen', // description
      undefined,          // startedAt
      undefined,          // finishedAt
      'ZOOM_CAMERA',      // textEvent
      'CONTROL_ACTION'    // intent
    ).subscribe({
      next: (res: any) => {
        console.log('Erfolg:', res);
        successSound.play().catch(err => console.error("Sound konnte nicht abgespielt werden: ", err));
      },
      error: (err: any) => {
        console.error('Fehler:', err);

        const savedSetting = localStorage.getItem('errorSoundEnabled');             //Wert wird aus dem localstorage der anwendung gelesen... -> so wie timestamp und language einstellungen die bereits vorhanden waren
        const isErrorSoundEnabled = (savedSetting != 'false');                      //Sound ist nur aus wenn wircklich auf false gesetzt... wenn an oder noch nie gesetzt spielt der sound

        if (isErrorSoundEnabled){
          errorSound.play().catch(soundErr => console.error("Error-Sound konnte nicht abgespielt werden: ", soundErr));
        }
      }
    });
  }
}


