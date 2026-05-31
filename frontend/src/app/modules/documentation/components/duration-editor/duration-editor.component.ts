import {
  ChangeDetectionStrategy,
  Component,
  Input,
  OnInit,
} from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { DatePipe } from '@angular/common';
import { Entry } from '../../models';
import { DownloadService } from '../../services';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-duration-editor',
  templateUrl: './duration-editor.component.html',
  styleUrls: ['./duration-editor.component.scss'],
  providers: [DatePipe],
})
export class DurationEditorComponent implements OnInit {
  @Input()
  entries: Entry[];

  @Input()
  title: string;

  @Input()
  disabled: boolean = false;

  text: string;

  constructor(
    private downloadService: DownloadService,
    private translate: TranslateService,
  ) {}

  ngOnInit(): void {
    this.text = this.buildDurationText(this.entries);
  }

  download() {
    this.downloadService.downloadDocumentation(
      this.buildDocumentationContent(),
      this.title,
    );
  }

  buildDurationText(entries: Entry[]) {
    let text = '';
    entries.forEach((entry) => {
      if (entry.description) {
        text += entry.buildText(this.translate);
      }
    });

    return text;
  }

  private buildDocumentationContent(): Map<
    string,
    { text: string; image: string }
  > {
    const content = new Map<string, { text: string; image: string }>();
    this.entries.forEach((entry) => {
      if (entry.description) {
        content.set(entry.elementId, {
          text: entry.buildText(this.translate),
          image: null,
        });
      }
    });
    return content;
  }
}
