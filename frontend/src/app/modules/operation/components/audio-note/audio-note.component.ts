import {ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnInit,} from '@angular/core';
import {BehaviorSubject} from 'rxjs';
import {AudioCacheService} from '../../../shared/services/audio-cache.service';

// checklist element that let's you record audio notes
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-audio-note',
  templateUrl: './audio-note.component.html',
  styleUrls: ['./audio-note.component.scss'],
})
export class AudioNoteComponent implements OnInit {
  @Input()
  elementId: string;

  @Input()
  disabled: boolean = false;

  chunks: any[] = [];
  mediaRecorder: any;
  audioURL$ = new BehaviorSubject<string>(null);

  isRecording = false;

  constructor(
    private cdr: ChangeDetectorRef,
    private audioCacheService: AudioCacheService,
  ) {}

  ngOnInit() {
    // todo this only works in https or localhost context. So a certificate for https or a tunnel is needed
    navigator.mediaDevices.getUserMedia({ audio: true }).then((stream) => {
      this.mediaRecorder = new MediaRecorder(stream);

      this.mediaRecorder.ondataavailable = (e: { data: any }) => {
        this.chunks.push(e.data);
      };

      this.mediaRecorder.onstop = () => {
        const blob = new Blob(this.chunks, { type: 'audio/ogg; codecs=opus' });

        const url = URL.createObjectURL(blob);

        this.audioCacheService.saveAudio(this.elementId, blob);

        this.audioURL$.next(url);
        this.chunks = [];
        this.cdr.detectChanges();
      };
    });
  }

  recordAudio() {
    if (this.isRecording) {
      this.mediaRecorder.stop();

      this.isRecording = false;
    } else {
      this.mediaRecorder.start();
      this.isRecording = true;
    }
  }
}
