import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'timestamp' })
export class TimestampPipe implements PipeTransform {
  transform(value: Date): string {
    if (value) {
      let date = new Date(value);
      let day = this.padZero(date.getDate());
      let month = this.padZero(date.getMonth() + 1);
      let year = this.padZero(date.getFullYear());
      let hours = this.padZero(date.getHours());
      let minutes = this.padZero(date.getMinutes());
      let seconds = this.padZero(date.getSeconds());
      return (
        day +
        '.' +
        month +
        '.' +
        year +
        ', ' +
        hours +
        ':' +
        minutes +
        ':' +
        seconds
      );
    }
    return '';
  }

  private padZero(value: number): string {
    return value < 10 ? `0${value}` : `${value}`;
  }
}
