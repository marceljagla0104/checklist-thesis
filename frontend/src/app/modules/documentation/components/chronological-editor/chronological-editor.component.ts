import {
  ChangeDetectionStrategy,
  Component,
  Input,
  OnInit,
} from '@angular/core';
import { catchError, of, take, tap } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';
import { DatePipe } from '@angular/common';
import { Entry } from '../../models';
import { DocumentationService, DownloadService } from '../../services';
import { ToastService } from '../../../shared/services';
import { AudioCacheService } from '../../../shared/services/audio-cache.service';
import { ImageCacheService } from '../../../shared/services/image-cache.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-chronological-editor',
  templateUrl: './chronological-editor.component.html',
  styleUrls: ['./chronological-editor.component.scss'],
  providers: [DatePipe],
})
export class ChronologicalEditorComponent implements OnInit {
  @Input()
  entries: Entry[];

  @Input()
  title: string;

  @Input()
  disabled: boolean = false;

  constructor(
    private documentationService: DocumentationService,
    private downloadService: DownloadService,
    private toastService: ToastService,
    private translate: TranslateService,
    private datePipe: DatePipe,
    private audioCache: AudioCacheService,
    private imageCache: ImageCacheService,
  ) {}

  ngOnInit(): void {}

  download() {
    const docu = this.buildDocumentationText();
    this.downloadService.downloadDocumentation(docu, this.title);
  }

  downloadWithoutTime() {
    const docu = this.buildDocumentationTextWithoutTimes();
    this.downloadService.downloadDocumentation(docu, this.title);
  }

  onSelectionChange($event: any, entry: Entry) {
    const div = document.getElementById(entry.id);
    const text = $event.target.value;

    const finishedAt = this.datePipe.transform(
      entry.finishedAt,
      'dd.MM.YYYY, HH:MM:ss',
    );

    if (entry.startedAt) {
      const startedAt = this.datePipe.transform(
        entry.startedAt,
        'dd.MM.YYYY, HH:MM:ss',
      );
      div.innerHTML = `${startedAt}: ${text}`;

      if (finishedAt) {
        div.innerHTML = div.innerHTML + `<br> ${finishedAt}: ${text}`;
      }
      return;
    }

    div.innerHTML = `${finishedAt}: ${text}`;
  }

  save() {
    let docu = this.buildDocumentationText();

    let text = '';
    docu.forEach((value) => {
      text += value.text + '\n';
    });

    this.documentationService
      .saveTextDocumentation(text)
      .pipe(
        tap((_) => {
          this.translate
            .get('SAVED_DOCUMENTATION')
            .pipe(take(1))
            .subscribe((res) => {
              this.toastService.showToast({
                duration: 2000,
                text: res,
              });
            });
        }),
        catchError(() => {
          this.translate
            .get('ERROR')
            .pipe(take(1))
            .subscribe((text) =>
              this.toastService.showToast({
                duration: 2000,
                text: text.message,
              }),
            );
          return of();
        }),
      )
      .subscribe();
  }

  private buildDocumentationText(): Map<
    string,
    { text: string; image: string }
  > {
    const docu = new Map<string, { text: string; image: string }>();

    this.entries.forEach((entry) => {
      if (this.imageCache.hasImage(entry.elementId)) {
        docu.set(entry.elementId, {
          text: '',
          image: this.imageCache.getImage(entry.elementId),
        });
      } else {
        let docuText = '';
        const div = document.getElementById(entry.id);
        if (div) {
          docuText += div.textContent + '\n';
        }
        const audioDiv = document.getElementById('audio-text-' + entry.id);
        if (audioDiv) {
          docuText += audioDiv.textContent + '\n';
        }

        let startedAt = this.datePipe.transform(
          entry.startedAt,
          'dd.MM.YYYY, HH:MM:ss',
        );
        let endedAt = this.datePipe.transform(
          entry.finishedAt,
          'dd.MM.YYYY, HH:MM:ss',
        );

        docuText = docuText.replace(startedAt, startedAt + '\n');
        docuText = docuText.replace(endedAt, '\n' + endedAt + '\n');
        docuText = docuText.replace(/^\s+/gm, '');

        docu.set(entry.elementId, { text: docuText, image: '' });
      }
    });
    return docu;
  }

  private buildDocumentationTextWithoutTimes(): Map<
    string,
    { text: string; image: string }
  > {
    const docu = new Map<string, { text: string; image: string }>();
    this.entries.forEach((entry) => {
      if (this.imageCache.hasImage(entry.elementId)) {
        docu.set(entry.elementId, {
          text: '',
          image: this.imageCache.getImage(entry.elementId),
        });
      } else {
        let docuText = '';
        const div = document.getElementById(entry.id);
        if (div) {
          let text = div.innerText;
          let startedAt = this.datePipe.transform(
            entry.startedAt,
            'dd.MM.YYYY, HH:MM:ss',
          );
          let endedAt = this.datePipe.transform(
            entry.finishedAt,
            'dd.MM.YYYY, HH:MM:ss',
          );
          text = text.replace(startedAt, '');
          text = text.replace(endedAt, '');
          docuText += text + '\n';

          const audioDiv = document.getElementById('audio-text-' + entry.id);
          if (audioDiv) {
            docuText += audioDiv.innerText + '\n';
          }

          docu.set(entry.elementId, { text: docuText, image: '' });
        }
      }
    });
    return docu;
  }

  buildAudioUrl(elementId: string) {
    const audio = this.audioCache.getAudio(elementId);
    return URL.createObjectURL(audio);
  }

  audioExists(elementId: string) {
    return this.audioCache.hasAudio(elementId);
  }

  imageExists(elementId: string) {
    return this.imageCache.hasImage(elementId);
  }

  getImage(elementId: string) {
    return this.imageCache.getImage(elementId);
  }
}
