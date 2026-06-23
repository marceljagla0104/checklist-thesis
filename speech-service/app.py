import os
import traceback
import shutil
from fastapi import FastAPI, UploadFile, File, HTTPException, Request
from fastapi.middleware.cors import CORSMiddleware
import whisper
import requests
import imageio_ffmpeg


try:
    # Pfad der installierten imageio-ffmpeg Exe holen
    real_ffmpeg_exe = imageio_ffmpeg.get_ffmpeg_exe()
    
    # Den aktuellen Ordner ermitteln (speech-service)
    current_dir = os.path.dirname(os.path.abspath(__file__))
    target_ffmpeg_exe = os.path.join(current_dir, "ffmpeg.exe")
    
    if not os.path.exists(target_ffmpeg_exe):
        print(f"Kopiere FFmpeg-Binärdatei lokal in den Projektordner.")
        shutil.copy(real_ffmpeg_exe, target_ffmpeg_exe)
        
    os.environ["PATH"] = current_dir + os.pathsep + os.environ["PATH"]
    print(f"FFmpeg erfolgreich lokal registriert in: {current_dir}")
except Exception as patch_error:
    print(f"FFmpeg-Patch fehlgeschlagen: {patch_error} !")
    traceback.print_exc()

app = FastAPI(title="CaMed Speech-to-Text Service (Whisper)")

# CORS aktivieren, damit dein Angular-Frontend (Port 4200) zugreifen darf
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Laden des kleinsten Modells 
print("Lade OpenAI Whisper Modell 'tiny'...")
model = whisper.load_model("tiny")
print("Modell erfolgreich geladen!")


@app.post("/api/speech/transcribe")
async def transcribe_audio(
    request: Request, 
    file: UploadFile = File(...),
    documentationId: str = "test-id",
    roomId: str = "room-01"
):
    temp_filename = "temp_recording.wav"
    print(f"\n--- [Empfang] Eingehender Audio-Upload für Doc-ID: {documentationId}, Raum: {roomId} ---")
    
    try:
        contents = await file.read()
        with open(temp_filename, "wb") as buffer:
            buffer.write(contents)
        
        print("Starte Whisper-Transkription (Inferenz läuft)...")
        result = model.transcribe(temp_filename, language="de")
        spoken_text = result.get("text", "").strip()
        print(f"=== [Whisper Ergebnis] Gesprochener Text: '{spoken_text}' ===")
        
        detected_intent = None
        normalized_text = spoken_text.lower()
        dictated_comment = "Automatisch abgehakt via VUI"
        
        if "zoom" in normalized_text or "kamera" in normalized_text:
            detected_intent = "ZOOM_CAMERA"
        elif "update" in normalized_text or "name" in normalized_text:
            detected_intent = "UPDATE_NAME"
        elif any(k in normalized_text for k in ["nächster", "naechster", "schritt", "weiter", "erledigt", "abgehakt", "bestätigen", "bestaetigen", "Arbeitsschritt"]):
            detected_intent = "NEXT_STEP"
        elif any(k in normalized_text for k in ["diktat", "notiz", "kommentar", "notieren"]):
            detected_intent = "ADD_COMMENT"
            clean_text = spoken_text
            for kw in ["diktat", "Diktat", "notiz", "Notiz", "kommentar", "Kommentar"]:
                clean_text = clean_text.replace(kw, "")
            dictated_comment = clean_text.strip(": ").strip()
            
        print(f"Erkannter Intent aus Analyse: {detected_intent}")
            
        if detected_intent:
            parsed_room_id = int(roomId) if roomId.isdigit() else roomId
            incoming_role = request.headers.get("x-role") 

            print(f"[Security-VUI] Empfangene Akteurs-Rolle aus Frontend: {incoming_role}")

            # Payload für das Java-Backend aufbauen
            payload = {
                "roomId": parsed_room_id,
                "elementId": "vui-voice-triggered",
                "description": dictated_comment,
                "textEvent": detected_intent,
                "intent": "CONTROL_ACTION",
                "role": incoming_role  # Injektion der Rolle direkt in den JSON-Body
            }

            DYNAMIC_JAVA_URL = f"http://localhost:8080/documentation/{documentationId}/room/{roomId}/entry/create"

            auth_header = request.headers.get("authorization")
            
            java_headers = {}
            if auth_header:
                java_headers["Authorization"] = auth_header
                print("[Security-Forward] Reiche Authorization-Header an Java-Backend weiter.")
            else:
                print("[Security-Forward] WARNUNG: Kein Authorization-Header zum Weiterleiten vorhanden.")

            print(f"Sende Payload an Java-Backend URL [{DYNAMIC_JAVA_URL}]: {payload}")
            try:
                response = requests.post(DYNAMIC_JAVA_URL, json=payload, headers=java_headers, timeout=5)
                print(f"Antwort vom Java-Backend erhalten: Status {response.status_code}")
                return {
                    "status": "success",
                    "text": spoken_text,
                    "intent": detected_intent,
                    "backend_status": response.status_code
                }
            except requests.exceptions.RequestException as e:
                print(f"!!! FEHLER bei Weiterleitung an Java !!!: {e}")
                return {"status": "backend_error", "text": spoken_text, "intent": detected_intent, "error": str(e)}
        
        print("Kein passender Befehl (Intent) im Text erkannt.")
        return {"status": "no_intent_recognized", "text": spoken_text, "intent": None}
        
    except Exception as e:
        print("!!! FEHLER IM PYTHON SERVER !!!")
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        if os.path.exists(temp_filename):
            os.remove(temp_filename)