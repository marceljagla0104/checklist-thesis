import {Injectable, OnDestroy} from '@angular/core';
import {Documentation, Entry} from '../models';
import {Observable, of, ReplaySubject, switchMap, take, tap} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {CreateDocumentationReq, DocumentationDTO, DocumentationListItemDTO,} from './api/documentation';
import {map} from 'rxjs/operators';
import {environment} from '../../../../environments/environment';
import { UserContextService } from '../../shared/services/user-context.service';

// service that handles documentation updates etc.
// caches documentation on load, so it's only loaded once and then updated through websocket messages
@Injectable()
export class DocumentationService implements OnDestroy {
  documentation$: ReplaySubject<Documentation> =
    new ReplaySubject<Documentation>(1); // caches the documentation

  constructor(
    private http: HttpClient,
    private userContext: UserContextService
  ){}

  private activeDocumentationId: string | null=null;

  setActiveDocumentationId(id: string){
    this.activeDocumentationId = id;

    localStorage.setItem('documentationId', id);
  }

  getActiveDocumentationId(): string | null {
    return this.activeDocumentationId || localStorage.getItem('documentationId');
  }

  ngOnDestroy(): void {
    this.documentation$.complete();
  }

  createDocumentation(operationId: string): Observable<string> {
    const roomId = window.sessionStorage.getItem('roomId');
    const req: CreateDocumentationReq = {
      operationId: operationId,
      roomId,
    };

    return this.http.post<string>(
      `http://${this.baseUrl()}/documentation/create`,
      req,
      { responseType: 'text' as 'json' },
    );
  }

  listDocumentationsByOperationId(operationId: string) {
    return this.http.get<DocumentationListItemDTO[]>(
      `http://${this.baseUrl()}/documentation/list-unfinished/${operationId}/room/${window.sessionStorage.getItem(
        'roomId',
      )}`,
    );
  }

  getDocumentation(documentationId: string): Observable<Documentation> {
    return this.http
      .get<DocumentationDTO>(
        `http://${this.baseUrl()}/documentation/${documentationId}`,
      )
      .pipe(
        tap(() => this.setActiveDocumentationId(documentationId)),
        map((dto) => {
          const entries = dto.entries.map((entry) => {
            return new Entry(
              entry.id,
              entry.elementId,
              entry.description,
              entry.textEvent,
              entry.phrases,
              entry.duration ? entry.duration : 0,
              entry.calculatedDuration ? entry.calculatedDuration : 0,
              entry.finishedAt,
              entry.startedAt,
            );
          });

          return new Documentation(
            dto.id,
            'Some title', //todo get rid of or get from backend
            entries,
          );
        }),
      );
  }

  cacheDocumentation(documentation: Documentation) {
    this.documentation$.next(documentation);
  }

  removeEntryFromCache(elementId: string) {
    this.documentation$
      .pipe(
        take(1),
        map((docu) => {
          const filteredEntries = docu.entries.filter(
            (entry) => entry.elementId !== elementId,
          );
          const updatedDocu = new Documentation(
            docu.id,
            docu.title,
            filteredEntries,
          );
          this.documentation$.next(updatedDocu);
        }),
      )
      .subscribe();
  }

  addEntryToCache(entry: Entry) {
    this.documentation$
      .pipe(
        take(1),
        map((docu) => {
          const updatedEntries = [...docu.entries, entry];
          const updatedDocu = new Documentation(
            docu.id,
            docu.title,
            updatedEntries,
          );
          this.documentation$.next(updatedDocu);
        }),
      )
      .subscribe();
  }

  callCirculating(circulatingTriggerId: string) {
    const req = {
      roomId: window.sessionStorage.getItem('roomId'),
      pathId: circulatingTriggerId,
      documentationId: window.localStorage.getItem('documentationId'),
    };
    return this.http.post<void>(
      `http://${this.baseUrl()}/sync/circulating/task/start`,
      req,
    );
  }

  getEntry(documentationId: string, elementId: string): Observable<Entry> {
    return this.http.get<Entry>(
      `http://${this.baseUrl()}/documentation/${documentationId}/entry/${elementId}`,
    );
  }

  loadEntry(elementId: string): Observable<Entry> {
    return this.documentation$.pipe(
      map((docu) =>
        docu.entries.find((entry) => entry.elementId === elementId),
      ),
      take(1),
    );
  }

  removeEntry(documentationId: string, elementId: string) {
    const roomId = window.sessionStorage.getItem('roomId');
    return this.http
      .post<Entry>(
        `http://${this.baseUrl()}/documentation/${documentationId}/room/${roomId}/entry/${elementId}/remove`,
        {},
      )
      .pipe(tap(() => this.removeEntryFromCache(elementId)));
  }

  // todo on entry changes update the entry in the documentation
  hasAnyEntry(elementIds: string[]): Observable<boolean> {
    return this.documentation$.pipe(
      switchMap((docu) => {
        if (docu) {
          const value = docu.hasAnyEntry(elementIds);
          return of(value);
        }
        return of(false);
      }),
    );
  }

  saveTextDocumentation(text: string) {
    const documentationId = window.localStorage.getItem('documentationId');
    const req = {
      documentationId,
      text,
    };
    return this.http.post<void>(
      `http://${this.baseUrl()}/documentation/save`,
      req,
    );
  }

  baseUrl() {
    return environment.url;
  }

  createOrUpdateEntry(
    documentationId: string,
    elementId: string,
    description: string,
    startedAt?: Date,
    finishedAt?: Date,
    textEvent?: string,
    intent?: string,      //neuer Parameter
  ): Observable<string> {
    const req = {
      elementId,
      textEvent,
      description,
      startedAt,
      finishedAt,
      intent,           //wird hier mit dem rest mitgeschickt
      role: this.userContext.getCurrentRole(),
      roomId: this.userContext.getCurrentRoomId(),
    };
    const roomId = window.sessionStorage.getItem('roomId');
    return this.http
      .post<string>(
        `http://${this.baseUrl()}/documentation/${documentationId}/room/${roomId}/entry/create`,
        req,
        { responseType: 'text' as 'json' },
      )
      .pipe(
        tap(() =>
          this.addEntryToCache(
            new Entry(
              elementId,
              elementId,
              description,
              textEvent,
              [],
              0,
              0,
              finishedAt,
              startedAt,
            ),
          ),
        ),
      );
  }
}
