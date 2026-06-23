import { Component, NgZone } from '@angular/core';

@Component({
  selector: 'app-page',
  templateUrl: './page.component.html',
  styleUrls: ['./page.component.scss']
})
export class PageComponent {
  isRecording = false;
  private mediaRecorder: MediaRecorder | null = null;
  private audioChunks: Blob[] = [];

  constructor(private ngZone: NgZone) {}

  async toggleRecording() {
    console.log('--- [Klick] Sprachsteuerungs-Button betätigt ---');
    
    if (this.isRecording) {
      console.log('Stoppe die Audio-Aufnahme...');
      if (this.mediaRecorder) {
        this.mediaRecorder.stop();
      }
      this.isRecording = false;
    } else {
      this.isRecording = true;
      this.audioChunks = [];
      console.log('Fordere Mikrofon-Berechtigung der Browsers an...');
      
      try {
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
        console.log('Mikrofon Zugriff TRUE -> Mediarecorder');
        
        this.ngZone.run(() => {
          this.mediaRecorder = new MediaRecorder(stream);
          
          this.mediaRecorder.ondataavailable = (event) => {
            this.audioChunks.push(event.data);
          };

          this.mediaRecorder.onstop = () => {
            console.log('Aufnahme beendet. Generiere AudioBlob');
            const audioBlob = new Blob(this.audioChunks, { type: 'audio/wav' });
            this.sendAudioToSpeechService(audioBlob);
          };

          this.mediaRecorder.start();
          console.log('Audio-Aufnahme gestartet...');
        });

      } catch (err) {
        console.error('!!! FEHLER BEIM MIKROFON-ZUGRIFF !!!', err);
        // Bei Fehlern oder Verweigerung den Zustand wieder zurücksetzen
        this.isRecording = false;
        alert('Mikrofonzugriff verweigert oder nicht möglich!');
      }
    }
  }

  private sendAudioToSpeechService(audioBlob: Blob) {
    const formData = new FormData();
    formData.append('file', audioBlob, 'recording.wav');

    // Kontext-Daten aus dem Speicher holen
    const dynamicDocumentationId = window.localStorage.getItem('documentationId') || 'default-demo-id';
    const dynamicRoomId = window.sessionStorage.getItem('roomId') || '1';

    console.log(`[VUI ARCHITEKTUR CONTEXT] Übermittele Doc-ID: ${dynamicDocumentationId} | Raum-ID: ${dynamicRoomId}`);
    
    const userRole = window.sessionStorage.getItem('role') || 'SURGEON';
    console.log(`[Security-VUI] Aktive Benutzerrolle aus SessionStorage: ${userRole}`);

    const headers = new Headers();
    headers.append('X-Role', userRole); // Übergabe als Custom-Header
    headers.append('Authorization', `Bearer ${userRole}`); // Fallback


    const pythonUrl = `http://localhost:8000/api/speech/transcribe?documentationId=${dynamicDocumentationId}&roomId=${dynamicRoomId}`;

    fetch(pythonUrl, {
      method: 'POST',
      body: formData,   // Die Audio-Datei im Body
      headers: headers  // NEU: Die Sicherheits-Header werden mitgeschickt!
    })
    .then(response => {
      if (!response.ok) {
        throw new Error('Server antwortete mit Statuscode ' + response.status);
      }
      return response.json();
    })
    .then(data => {
      console.log('=== [ERFOLG] Antwort vom Whisper-Service ===', data);
      
      // Validierung der geschlossenen Pipeline-Kette unter Beachtung von Autorisierungsfehlern
      if (data.intent && data.backend_status === 200) {
        console.log('Erfolgreiche Sprachsteuerungstransaktion! Spiele Bestätigungston...');
        this.playAudioSignal('success');
      } else {
        console.warn('Transaktionswarnung! Backend-Status:', data.backend_status, 'Intent:', data.intent);
        this.playAudioSignal('error');
      }
    })
    .catch(error => {
      console.error('Verbindungsfehler zum Whisper-Service:', error);
      this.playAudioSignal('error');
    });
  }


  private playAudioSignal(type: 'success' | 'error') {
    // Zustand aus dem localStorage auslesen
    const soundEnabled = localStorage.getItem('errorSoundEnabled') !== 'false';
    
    if (type === 'error' && !soundEnabled) {
      console.log('[-] Akustische Fehlerrückmeldung blockiert: Deaktiviert in den Benutzereinstellungen.');
      return;
    }

    try {
      const audio = new Audio();
      
      const assetPath = type === 'success' ? '/assets/sounds/success.mp3' : '/assets/sounds/error.mp3';
      
      console.log(`[Audio-Prüfung] Versuche Sound zu laden von: ${window.location.origin}${assetPath}`);
      
      audio.src = assetPath;
      audio.load();
      
      audio.play()
        .then(() => console.log(`[+] Akustisches Signal [${type.toUpperCase()}] erfolgreich wiedergegeben.`))
        .catch(playErr => console.error('[-] Browser hat Autoplay blockiert oder Datei fehlt:', playErr));
        
    } catch (soundError) {
      console.error('[-] Kritischer Fehler bei der Audio-Initialisierung:', soundError);
    }
  }
}


