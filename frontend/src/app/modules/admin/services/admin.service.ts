import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { DocumentationListItemDTO, OperationListItemDTO } from './api/admin';
import { environment } from '../../../../environments/environment';

@Injectable()
export class AdminService {
  constructor(private http: HttpClient) {}

  public listOperations(): Observable<OperationListItemDTO[]> {
    return this.http.get<OperationListItemDTO[]>(
      `http://${environment.url}/operations`,
    );
  }

  public listDocumentations(): Observable<DocumentationListItemDTO[]> {
    return this.http.get<DocumentationListItemDTO[]>(
      `http://${environment.url}/documentations`,
    );
  }

  uploadOperation(selectedFile: File) {
    const formData = new FormData();
    formData.append('file', selectedFile, selectedFile.name);

    return this.http.post(`http://${environment.url}/operation`, formData, {
      responseType: 'text',
    });
  }

  deleteOperation(id: string) {
    return this.http.post<void>(
      `http://${environment.url}/operation/${id}/delete`,
      {},
    );
  }
}
