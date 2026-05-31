import {Injectable} from '@angular/core'; //this class handles audio recordings to show in the documentation tab

//this class handles audio recordings to show in the documentation tab
//the blobs could also be send to the backend and saved
//but this is not done yet to save resources
@Injectable({
  providedIn: 'root',
})
export class AudioCacheService {
  cache: Map<string, Blob> = new Map<string, Blob>();

  getAudio(elementId: string): Blob {
    return this.cache.get(elementId);
  }

  saveAudio(elementId: string, audio: Blob): void {
    this.cache.set(elementId, audio);
  }

  hasAudio(elementId: string) {
    return this.cache.has(elementId);
  }
}
