import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { Operation } from '../models';
import { OperationDTO, OperationListItemDTO } from './api/operation';
import { map } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';

@Injectable()
export class OperationService {
  constructor(private http: HttpClient) {}

  public listOperations(): Observable<OperationListItemDTO[]> {
    return this.http.get<OperationListItemDTO[]>(
      `http://${environment.url}/operations`,
    );
  }

  public getOperation(id: string): Observable<Operation> {
    return this.http
      .get<OperationDTO>(`http://${environment.url}/operation/${id}`)
      .pipe(
        map((surgeonOperation) => {
          return new Operation(surgeonOperation);
        }),
      );
  }
}
