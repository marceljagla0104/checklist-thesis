# VUI Integration Pipeline for CaMed Checklists
### Bachelor's Thesis by Marcel Jagla – Reutlingen University (Faculty of Computer Science)

This repository contains the runnable and fully evaluated prototype of the service-oriented three-tier architecture for voice-based control of medical checklists in the operating room.

---

## System Prerequisites
Before the pipeline is started, the following tools must be installed on the host system (Windows):
- **Docker Desktop** (for the encapsulated database infrastructure)
- **Node.js & Angular CLI** (v16+ for the frontend)
- **Python 3.10+** (for the AI speech processing)
- **Java OpenJDK 17 & Gradle** (for the reactive core backend)

---

## Component Startup Order

Follow this exact order to ensure a fault-free connection of the event buses and network sockets.

### 1. Database
The MongoDB runs isolated and preconfigured in a container environment. Before executing these commands, open Docker Desktop and connect to the database.
- Execute command in the root directory: docker-compose up -d
- The database is then accessible locally on port 27017.

### 2. AI Speech Service
The Python speech service processes transient audio blobs via OpenAI Whisper (tiny). It runs natively on the host to avoid additional virtualization overhead under Windows (WSL2).

- Execute commands in the "speech-service" folder (Initial Installation!!!):
    cd speech-service
    python -m venv venv
    .\venv\Scripts\activate
    pip install -r requirements.txt
    uvicorn app:app --reload --port 8000

- Execute commands in the "speech-service" folder (If the system has already run before):
    cd speech-service
    uvicorn app:app --reload --port 8000

Troubleshooting: Local FFmpeg Installation via Console
Whisper strictly requires FFmpeg for audio decoding. If FFmpeg is not installed globally on the system, simply execute this single command in the Windows PowerShell inside the "speech-service" folder:
mkdir ffmpeg; cd ffmpeg; Invoke-WebRequest -Uri "https://www.gyan.dev/ffmpeg/builds/ffmpeg-release-essentials.zip" -OutFile "ffmpeg.zip"; Expand-Archive -Path "ffmpeg.zip" -DestinationPath "."; Move-Item .\ffmpeg-*\bin\* .; Remove-Item "ffmpeg.zip", .\ffmpeg-* -Recurse; cd ..

The runtime patch implemented in the Python code now automatically registers this relative path (/speech-service/ffmpeg/) upon application startup.

### 3. Core Backend
The core processes data streams non-blocking based on the Netty Engine and Project Reactor (Mono / Flux). Role-Based Access Control (RBAC) is controlled declaratively via the central meta.yaml file.

- Execute commands in the "backend" folder:
    cd backend
    .\gradlew bootRun
- The backend starts on port 8080.

### 4. Frontend
The user interface captures audio data transiently via a push-to-talk process and controls eyes-free audio playback (success.mp3 / error.mp3) via the HTML5 Web Audio API.

- Execute commands in the "frontend" folder:
    cd frontend
    npm install
    npm start
- Then open http://localhost:4200 in the browser.

---

### Alternatively
A start_all_services.bat file can be found in the root directory. Please note that you need to run the installation first. The start_all_service.bat wont work without proper installation in first place. Then simply launch Docker Desktop and click on start_all_services.bat.

---

