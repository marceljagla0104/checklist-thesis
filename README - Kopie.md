
# VUI-Integrationspipeline für CaMed-Checklisten
### Bachelorarbeit von Marcel Jagla – Hochschule Reutlingen (Fakultät Informatik)

Dieses Repository enthält den lauffähigen und vollständig evaluierten Prototypen der serviceorientierten Drei-Schichten-Architektur zur sprachbasierten Steuerung von medizinischen Checklisten im Operationssaal.

---

## System-Voraussetzungen
Bevor die Pipeline gestartet wird, müssen folgende Werkzeuge auf dem Host-System (Windows) installiert sein:
- **Docker Desktop** (für die gekapselte Datenbank-Infrastruktur)
- **Node.js & Angular CLI** (v16+ für das Frontend)
- **Python 3.10+** (für die KI-Sprachverarbeitung)
- **Java OpenJDK 17 & Maven** (für das reaktive Core-Backend)

---

## Start-Reihenfolge der Komponenten

Folgen Sie exakt dieser Reihenfolge, um eine fehlerfreie Verbindung der Event-Busse und Netzwerk-Sockets zu gewährleisten.

### 1.Datenbank
Die MongoDB läuft isoliert und vorkonfiguriert in einer Container-Umgebung. Vor dem ausführen dieser befehle Docker Desktop öffnen und mit der Datenbank verbinden
- Befehl im Hauptverzeichnis ausführen: docker-compose up -d
- Die Datenbank ist anschließend lokal auf Port 27017 erreichbar.

### 2.KI-Sprachdienst
Der Python-Speech-Service verarbeitet die transienten Audio-Blobs via OpenAI Whisper (tiny). Er wird nativ auf dem Host ausgeführt, um zusätzlichen Virtualisierungs-Overhead unter Windows (WSL2) zu vermeiden.

- Befehle im Ordner "speech-service" ausführen:  (Erstinstallation!!!)
    cd speech-service
    python -m venv venv
    .\venv\Scripts\activate
    pip install -r requirements.txt
    uvicorn app:app --reload --port 8000
- Befehle im Ordner "speech-service" ausführen: (Falls System schon gelaufen ist)
    cd speech-service
    uvicorn app:app --reload --port 8000

Troubleshooting: Lokale FFmpeg-Installation via Konsole 
Whisper benötigt zwingend FFmpeg zur Audio-Dekodierung. Falls FFmpeg auf dem System nicht global installiert ist, führen Sie in der Windows PowerShell im Ordner "speech-service" einfach diesen einzigen Befehl aus:
mkdir ffmpeg; cd ffmpeg; Invoke-WebRequest -Uri "https://www.gyan.dev/ffmpeg/builds/ffmpeg-release-essentials.zip" -OutFile "ffmpeg.zip"; Expand-Archive -Path "ffmpeg.zip" -DestinationPath "."; Move-Item .\ffmpeg-*\bin\* .; Remove-Item "ffmpeg.zip", .\ffmpeg-* -Recurse; cd ..

Der im Python-Code implementierte Runtime-Patch registriert diesen relativen Pfad (/speech-service/ffmpeg/) beim Applikationsstart nun vollautomatisch.

### 3. Core-Backend
Das Herzstück verarbeitet die Datenströme non-blocking auf Basis der Netty Engine und Project Reactor (Mono / Flux). Die rollenbasierte Zugriffskontrolle (RBAC) wird deklarativ über die zentrale Datei meta.yaml gesteuert.

- Befehle im Ordner "backend" ausführen:
    cd backend
    .\gradlew bootRun
- Das Backend startet auf Port 8080.

### 4. Frontend
Das User-Interface erfasst Audiodaten transient über ein Push-to-Talk-Verfahren und steuert die blickfreie Audiowiedergabe (success.mp3 / error.mp3) über die HTML5 Web Audio API.

- Befehle im Ordner "frontend" ausführen:
    cd frontend
    npm install
    ng serve
- Öffnen Sie anschließend http://localhost:4200 im Browser.


### Alternativ
Im Hauptverzeichniss findet sich eine .bat datei

