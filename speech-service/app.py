import os
from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.middleware.cors import CORSMiddleware
import whisper
import requests

app = FastAPI(title="CaMed Speech-to-Text Service (Whisper)")

# CORS aktivieren, damit dein Angular-Frontend (Port 4200) zugreifen darf
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Laden des kleinsten Modells (Optimiert für Latenz und Laborumgebung)
print("Lade OpenAI Whisper Modell 'tiny'...")
model = whisper.load_model("tiny")
print("Modell erfolgreich geladen!")

# URL deines Java-Backends (Passe den Port an, falls dein Java-Backend nicht auf 8080 läuft)
JAVA_BACKEND_URL = "http://localhost:8080/api/documentation/execute"

@app.post("/api/speech/transcribe")
async def transcribe_audio(file: UploadFile = File(...)):
    temp_filename = "temp_recording.wav"
    
    try:
        #Temporäres Speichern der empfangenen Audio-Datei
        with open(temp_filename, "wb") as buffer:
            buffer.write(await file.read())
        
        #Whisper Inferenz (Spracherkennung erzwingen auf Deutsch)
        result = model.transcribe(temp_filename, language="de")
        spoken_text = result.get("text", "").strip()
        print("Erkannter Text: {spoken_text}")
        
        #Intent-Analyse basierend auf Schlüsselwörtern
        detected_intent = None
        normalized_text = spoken_text.lower()
        
        if "zoom" in normalized_text or "kamera" in normalized_text:
            detected_intent = "ZOOM_CAMERA"
        elif "update" in normalized_text or "name" in normalized_text:
            detected_intent = "UPDATE_NAME"
            
        #Weiterleitung an das Java-Backend per POST-Request
        if detected_intent:
            payload = {
                "documentationId": "test-element-01",  # Dummy oder dynamisch
                "textEvent": detected_intent,
                "intent": "CONTROL_ACTION"
            }
            try:
                response = requests.post(JAVA_BACKEND_URL, json=payload, timeout=5)
                return {
                    "status": "success",
                    "text": spoken_text,
                    "intent": detected_intent,
                    "backend_status": response.status_code
                }
            except requests.exceptions.RequestException as e:
                return {
                    "status": "backend_error",
                    "text": spoken_text,
                    "intent": detected_intent,
                    "error": str(e)
                }
        
        return {"status": "no_intent_recognized", "text": spoken_text, "intent": None}
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
        
    finally:
        #Temporäre Datei löschen
        if os.path.exists(temp_filename):
            os.remove(temp_filename)