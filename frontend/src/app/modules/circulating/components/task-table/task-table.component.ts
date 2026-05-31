import { Component, Input } from '@angular/core';
import { CirculatingTask } from '../../models';
import { TaskService } from '../../services';

@Component({
  selector: 'app-circulating-task-table',
  templateUrl: './task-table.component.html',
  styleUrls: ['./task-table.component.scss'],
})
export class TaskTableComponent {
  @Input()
  tasks: CirculatingTask[];

  displayedColumns: string[] = ['room', 'description', 'startedAt', 'checked'];

  constructor(private taskService: TaskService) {}

  checkTask($event: any, task: CirculatingTask) {
    const checked = $event.target.checked;
    if (checked) {
      this.taskService
        .finishTask(task.entryId, task.roomId, task.documentationId)
        .subscribe();
    }
  }
}
