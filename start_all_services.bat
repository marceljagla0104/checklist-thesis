@echo off
title CaMed System Orchestrator
echo ========================================================
echo   Bereinige System-Ports und initialisiere CaMed Pipeline
echo ========================================================
echo.

:: 1. STARTE MONGODB VIA DOCKER
echo [1/5] Starte MongoDB Container via Docker Compose...
docker-compose up -d db

timeout /t 2 /nobreak > nul

:: 2. GHOST-PROZESSE KILLEN (Port 8080 automatisch freiräumen)
echo [2/5] Pruefe Port 8080 und beende alte Java-Instanzen...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080 ^| findstr LISTENING') do taskkill /f /pid %%a 2>nul

timeout /t 1 /nobreak > nul

:: 3. STARTE JAVA SPRING BOOT BACKEND
echo [3/5] Starte Java-Backend (Port 8080)...
start "Java Backend Server" cmd /k "cd backend && gradlew bootRun"

timeout /t 5 /nobreak > nul

:: 4. STARTE FASTAPI WHISPER MIDDLEWARE
echo [4/5] Starte Python Speech-Service (Port 8000)...
start "Python Whisper Service" cmd /k "cd speech-service && uvicorn app:app --reload --port 8000"

timeout /t 3 /nobreak > nul

:: 5. STARTE ANGULAR FRONTEND
echo [5/5] Starte Angular Frontend (Port 4200)...
start "Angular Frontend Client" cmd /k "cd frontend && npm start"

echo.
echo ========================================================
echo   ALLE SERVICES WURDEN ERFOLGREICH INITIALISIERT!
echo   Die Docker-Datenbank (MongoDB) laeuft im Hintergrund.
echo   Dieses Hauptfenster kann jetzt minimiert werden.
echo ========================================================
pause