import { WebsocketMsgType } from '../services/api/websockets';
import { zip } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';

export class Documentation {
  id: string;
  title: string;
  entries: Entry[];

  constructor(id: string, title: string, entries: Entry[]) {
    this.id = id;
    this.title = title;
    this.entries = entries;
  }

  hasAnyEntry(elementIds: string[]): boolean {
    return this.entries.some((entry) => elementIds.includes(entry.elementId));
  }
}

export class Entry {
  id: string;
  elementId: string;
  description: string;
  textEvent?: string;
  phrases: string[];
  descriptions: Descriptions;
  startedAt?: Date;
  finishedAt?: Date;
  duration?: number; //todo get from backend
  calcDuration?: number; // todo get from backend

  constructor(
    id: string,
    elementId: string,
    entryText: string,
    textEvent: string,
    phrases: string[],
    duration: number,
    calcDuration: number,
    endedAt?: Date,
    startedAt?: Date,
  ) {
    this.id = id;
    this.elementId = elementId;
    this.description = entryText;
    this.textEvent = textEvent;
    this.finishedAt = endedAt;
    this.startedAt = startedAt;
    this.duration = duration;
    this.calcDuration = calcDuration;
    this.phrases = phrases;
    this.descriptions = new Descriptions();
  }

  hasEnd() {
    return !!this.finishedAt;
  }

  getDurationAsText() {
    return this.sToHMS(this.duration);
  }

  getCalcDurationAsText() {
    return this.sToHMS(this.calcDuration);
  }

  getDescription() {
    return this.phrases.length === 1 ? this.phrases[0] : this.description;
  }

  private sToHMS(seconds: number) {
    // duration calculation in backend is not optimal,
    // when no start and end time is given
    // so this is a quickfix for negative durations
    if (seconds < 0) {
      return '00:00:00';
    }

    const hours = seconds / 3600;
    seconds = seconds % 3600;
    const minutes = seconds / 60;
    seconds = seconds % 60;

    return (
      this.padZero(Math.floor(hours)) +
      ':' +
      this.padZero(Math.floor(minutes)) +
      ':' +
      this.padZero(Math.floor(seconds))
    );
  }

  private padZero(value: number): string {
    return value < 10 ? `0${value}` : `${value}`;
  }

  buildText(translate: TranslateService) {
    let text = '';
    if (this.hasEnd()) {
      text += this.getDescription() + '\n';
      const duration = this.getDurationAsText();
      const calcDuration = this.getCalcDurationAsText();

      zip(
        translate.get('TOTAL_DURATION'),
        translate.get('CALCULATED_DURATION'),
      ).subscribe(([totalDuration, calculatedDuration]) => {
        text += '    ' + totalDuration + ': ' + duration + '\n';
        text += '    ' + calculatedDuration + ': ' + calcDuration + '\n';
      });
      text += '\n';
    }
    return text;
  }
}

export class Descriptions {
  text: Description[];
  audio: Description[];

  constructor() {
    this.text = [];
    this.audio = [];
  }
}

export class Description {
  content: string | Blob;
  date: Date;
}

export class EntryChange {
  entryId: string;
  elementId: string;
  type: WebsocketMsgType;
  description: string;
  textEvent?: string;
  startedAt: Date;
  finishedAt: Date;
}
