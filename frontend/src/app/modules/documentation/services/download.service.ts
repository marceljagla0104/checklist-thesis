import {Injectable} from '@angular/core';
import {Document, ImageRun, Packer, Paragraph, TextRun} from 'docx';

// used to download documentation text as formatted docx file
@Injectable()
export class DownloadService {
  downloadDocumentation(
    documentationContent: Map<string, { text: string; image: string }>,
    title: string,
  ) {
    const doc$ = this.buildDocx(documentationContent);

    doc$.then((d) => {
      Packer.toBlob(d).then((blob) => {
        this.saveAs(blob, title);
      });
    });
  }

  saveAs(blob: Blob, fileName: string) {
    const link = document.createElement('a');
    const blobUrl = URL.createObjectURL(blob);
    link.href = blobUrl;
    const timestamp = new Date().toISOString();
    link.download = fileName + '_' + timestamp + '.docx';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(blobUrl);
  }

  async buildDocx(
    documentationContent: Map<string, { text: string; image: string }>,
  ): Promise<Document> {
    const paragraphs: Paragraph[] = [];

    for (const [key, value] of documentationContent.entries()) {
      if (value.text) {
        const para = value.text.split('\n').map(
          (text) =>
            new Paragraph({
              children: [new TextRun(text)],
            }),
        );
        paragraphs.push(...para);
      } else if (value.image) {
        const imageParagraph = await this.urlsToParagraph(value.image);
        paragraphs.push(imageParagraph);
      }
    }

    return new Document({
      sections: [
        {
          properties: {},
          children: [...paragraphs],
        },
      ],
    });
  }

  private async urlsToParagraph(image: string) {
    const base64String = image.split(',')[1];
    const binaryString = atob(base64String);

    const uint8Array = new Uint8Array(binaryString.length);
    for (let i = 0; i < binaryString.length; i++) {
      uint8Array[i] = binaryString.charCodeAt(i);
    }

    const img = new Image();
    img.src = image;

    await new Promise((resolve) => {
      img.onload = resolve;
    });

    const width = img.width;
    const height = img.height;

    return new Paragraph({
      children: [
        new ImageRun({
          data: uint8Array,
          transformation: {
            width: width / 2,
            height: height / 2,
          },
        }),
      ],
    });
  }
}
