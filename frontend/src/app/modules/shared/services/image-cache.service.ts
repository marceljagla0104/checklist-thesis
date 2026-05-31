import {Injectable} from '@angular/core'; //this class handles audio recordings to show in the documentation tab

//this class handles images to show in the documentation tab
//the blobs could also be send to the backend and saved
//but this is not done yet to save resources
@Injectable({
  providedIn: 'root',
})
export class ImageCacheService {
  cache: Map<string, string> = new Map<string, string>();

  getImage(elementId: string): string {
    return this.cache.get(elementId);
  }

  saveImage(elementId: string, image: string): void {
    this.cache.set(elementId, image);
  }

  hasImage(elementId: string) {
    return this.cache.has(elementId);
  }
}
