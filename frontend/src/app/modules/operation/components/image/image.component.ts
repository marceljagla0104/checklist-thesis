import {ChangeDetectionStrategy, Component, Input, OnInit,} from '@angular/core';
import {Element, Image} from '../../models';
import {Role} from '../../../shared/models';
import {environment} from '../../../../../environments/environment';

// displays images of the element
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-image',
  templateUrl: './image.component.html',
  styleUrls: ['./image.component.scss'],
})
export class ImageComponent implements OnInit {
  @Input()
  element: Element;

  active: boolean;

  images: Image[] = [];

  constructor() {}

  ngOnInit(): void {
    const role = window.sessionStorage.getItem('role') as Role;

    if (role === Role.SURGEON) {
      this.images = this.element.surgeonImages;
    }

    if (role === Role.STUDENT) {
      this.images = this.element.studentImages;
      this.active = true;
    }
  }

  buildURL(url: string): string {
    return `http://${environment.url}/image${url}`;
  }

  adjustContainerWidth(img: HTMLImageElement, container: HTMLDivElement) {
    const imgWidth = img.width;
    console.log(container);
    container.style.width = `${imgWidth}px`;
  }
}
