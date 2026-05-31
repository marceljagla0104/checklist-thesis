import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {filter, Observable, Subject, tap} from 'rxjs';
import {CallCirculatingReq} from './api/requests';
import {environment} from '../../../../environments/environment';
import {Entry, EntryChange} from '../../documentation/models';
import {ChecklistWebsocketMsg, WebsocketMsgType,} from '../../documentation/services/api/websockets';
import {map} from 'rxjs/operators';
import {ToastService} from './toast.service';
import {DocumentationService} from '../../documentation/services';

// service that handles the websocket connection to the server and listens to messages
@Injectable({
  providedIn: 'root',
})
export class SyncService {
  socket: WebSocket;

  private message$ = new Subject<ChecklistWebsocketMsg>();

  private retryCount = 0;
  private maxRetryAttempts = 5;
  private connectionClosedOnPurpose = false;

  constructor(
    private http: HttpClient,
    private toastService: ToastService,
    private documentationService: DocumentationService,
  ) {}

  onTabChange() {
    return this.message$.asObservable().pipe(
      filter((change) => change.type === WebsocketMsgType.CHANGE_TAB),
      map((msg) => msg.payload.tabName),
    );
  }

  onEntryUpdated(elementId: string): Observable<EntryChange> {
    return this.message$.asObservable().pipe(
      filter((change) => change.type === WebsocketMsgType.ENTRY_UPDATED),
      map((msg) => this.msgToEntryChange(msg)),
      filter((change) => change.elementId === elementId),
      tap((change) => {
        const entry = new Entry(
          change.entryId,
          change.elementId,
          change.description,
          change.textEvent,
          [],
          0,
          0,
          change.finishedAt,
          change.startedAt,
        );
        this.documentationService.addEntryToCache(entry); // update cache
      }),
    );
  }

  onEntryRemoved(elementId: string): Observable<EntryChange> {
    return this.message$.asObservable().pipe(
      filter((change) => change.type === WebsocketMsgType.ENTRY_REMOVED),
      map((msg) => this.msgToEntryChange(msg)),
      filter((change) => change.elementId === elementId),
    );
  }

  onCirculatingTaskDone(elementId: string): Observable<EntryChange> {
    return this.message$.asObservable().pipe(
      filter(
        (change) => change.type === WebsocketMsgType.CIRCULATING_TASK_DONE,
      ),
      map((msg) => this.msgToEntryChange(msg)),
      filter((change) => change.elementId === elementId),
    );
  }

  onCirculatingTaskStarted(elementId: string): Observable<EntryChange> {
    return this.message$.asObservable().pipe(
      filter(
        (change) => change.type === WebsocketMsgType.CIRCULATING_TASK_STARTED,
      ),
      map((msg) => this.msgToEntryChange(msg)),
      filter((change) => change.elementId === elementId),
    );
  }

  callCirculatingNurse(description: string): Observable<any> {
    const req: CallCirculatingReq = {
      description,
      documentationId: window.localStorage.getItem('documentationId'),
      roomId: window.sessionStorage.getItem('roomId'),
    };

    return this.http.post(
      `http://${environment.url}/sync/circulating/call`,
      req,
    );
  }

  changeTab(tabName: string): Observable<void> {
    const req = {
      documentationId: window.localStorage.getItem('documentationId'),
      roomId: window.sessionStorage.getItem('roomId'),
      tabName,
    };
    return this.http.post<void>(`http://${environment.url}/sync/tab`, req);
  }

  baseUrl() {
    return environment.url;
  }

  private msgToEntryChange(msg: ChecklistWebsocketMsg) {
    const startedAt = msg.payload.startedAt
      ? new Date(msg.payload.startedAt)
      : null;
    const finishedAt = msg.payload.finishedAt
      ? new Date(msg.payload.finishedAt)
      : null;

    const entryChange: EntryChange = {
      entryId: msg.payload.entryId as string,
      elementId: msg.payload.id as string,
      type: msg.type,
      description: msg.payload.description as string,
      textEvent: msg.payload.textEvent as string,
      startedAt,
      finishedAt,
    };
    return entryChange;
  }

  public connectWebSocket() {
    this.closeConnection(); // if connection already exists, close it to retry
    const roomId = window.sessionStorage.getItem('roomId');
    this.socket = new WebSocket(
      `ws://${this.baseUrl()}/checklist?room=${roomId}`,
    );

    this.socket.onopen = (event) => {
      console.log('Connection opened');
      // Reset retry count on successful connection
      this.retryCount = 0;
    };

    this.socket.onmessage = (event: MessageEvent<string>) => {
      const msg = JSON.parse(event.data) as ChecklistWebsocketMsg;
      console.log('Received message: ', msg);

      if (msg.roomId !== roomId) {
        console.error('roomId not matching', msg.roomId, roomId);
        return;
      }

      this.message$.next(msg);
    };

    this.socket.onerror = (error) => {
      console.log('Error occurred: ', error);
    };

    this.socket.onclose = (event) => {
      console.log('Connection closed');
      if (this.connectionClosedOnPurpose) {
        this.connectionClosedOnPurpose = false;
        return;
      }
      this.retryConnection();
    };
  }

  public closeConnection() {
    if (
      this.socket &&
      this.socket.readyState !== WebSocket.CLOSED &&
      this.socket.readyState !== WebSocket.CLOSING
    ) {
      this.socket.close();
      this.connectionClosedOnPurpose = true;
    }
  }

  private retryConnection() {
    // Increment retry count
    this.retryCount++;

    if (this.retryCount <= this.maxRetryAttempts) {
      // Retry connection after a delay
      console.log(
        `Retrying connection... (Attempt ${this.retryCount}/${this.maxRetryAttempts})`,
      );
      setTimeout(() => {
        this.connectWebSocket();
      }, 5000);
    } else {
      console.log('Max retry attempts reached. Connection failed.');
      this.toastService.showToast({
        text: 'Unable to reach server',
        duration: 2000,
        color: 'warn',
      });
    }
  }
}
