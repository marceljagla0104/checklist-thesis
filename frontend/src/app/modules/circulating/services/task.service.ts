import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, ReplaySubject } from 'rxjs';
import { CirculatingTask } from '../models';
import { ChecklistWebsocketMsg, WebsocketMsgType } from './api/websockets';
import { environment } from '../../../../environments/environment';
import { ToastService } from '../../shared/services';

@Injectable()
export class TaskService {
  private ROOM_ID: string = 'CIRCULATING';

  socket: WebSocket;
  tasks: CirculatingTask[] = [];

  tasks$ = new ReplaySubject<CirculatingTask[]>(1);

  private retryCount = 0;
  private maxRetryAttempts = 5;

  constructor(
    private http: HttpClient,
    private toastService: ToastService,
  ) {
    this.connectWebSocket();
  }

  private connectWebSocket() {
    this.socket = new WebSocket(
      `ws://${environment.url}/checklist?room=${this.ROOM_ID}`,
    );

    this.socket.onopen = (event) => {
      console.log('Connection opened task');
      // Reset retry count on successful connection
      this.loadUnfinishedTasks().subscribe({
        next: (tasks) => {
          this.tasks = tasks;
          this.tasks$.next(tasks);
        },
        error: () =>
          this.toastService.showToast({
            text: 'Error loading Tasks',
            duration: 3000,
            color: 'warn',
          }),
      });
      this.retryCount = 0;
    };

    this.socket.onmessage = (event: MessageEvent<string>) => {
      const data = JSON.parse(event.data) as ChecklistWebsocketMsg;

      if (!Object.values(WebsocketMsgType).includes(data.type)) {
        return;
      }

      console.log('Received message: ', data);
      switch (data.type) {
        case WebsocketMsgType.CALL_CIRCULATING:
          this.addOrOverwriteTask(data);
          break;

        case WebsocketMsgType.CIRCULATING_TASK_DONE:
          this.checkTask(data);
          break;
        default:
          return;
      }

      window.addEventListener('beforeunload', () => this.tasks$.complete());
    };

    this.socket.onerror = (error) => {
      console.log('Error occurred: ', error);
    };

    this.socket.onclose = (event) => {
      console.log('Connection closed');
      this.retryConnection();
    };
  }

  loadUnfinishedTasks(): Observable<CirculatingTask[]> {
    return this.http.get<CirculatingTask[]>(
      `http://${environment.url}/sync/circulating/tasks/unfinished`,
    );
  }

  getTasks(): Observable<CirculatingTask[]> {
    return this.tasks$.asObservable();
  }

  finishTask(
    entryId: string,
    room: string,
    documentationId: string,
  ): Observable<void> {
    const req = {
      entryId,
      roomId: room,
      documentationId: documentationId,
    };
    return this.http.post<void>(
      `http://${environment.url}/sync/circulating/task/finish`,
      req,
    );
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
      }, 5000); // Retry after 5 seconds (adjust the delay as needed)
    } else {
      console.log('Max retry attempts reached. Connection failed.');
      this.toastService.showToast({
        text: 'Unable to reach server',
        duration: 2000,
        color: 'warn',
      });
    }
  }

  private addOrOverwriteTask(data: ChecklistWebsocketMsg) {
    const task: CirculatingTask = {
      entryId: data.payload.entryId,
      documentationId: data.documentationId,
      description: data.payload.description,
      roomId: data.payload.roomId,
      startedAt: new Date(data.payload.startedAt),
    };

    const existingTaskIndex = this.tasks.findIndex(
      (existingTask) => existingTask.entryId === task.entryId,
    );

    if (existingTaskIndex !== -1) {
      // If the task already exists, overwrite it
      console.log('overwrite');
      this.tasks[existingTaskIndex] = task;
    } else {
      // If it doesn't, add the new task to the tasks array
      this.tasks = [task, ...this.tasks];
    }

    this.tasks$.next([...this.tasks]);
  }

  private checkTask(data: ChecklistWebsocketMsg) {
    const taskIndex = this.tasks.findIndex(
      (task) => task.entryId === data.payload.entryId,
    );

    if (taskIndex === -1) {
      return;
    }
    const task = this.tasks[taskIndex];
    task.finishedAt = new Date(data.payload.finishedAt);

    this.tasks[taskIndex] = task;

    console.log(this.tasks);
    this.tasks$.next([...this.tasks]);
  }
}
