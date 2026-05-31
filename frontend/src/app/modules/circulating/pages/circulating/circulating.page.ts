import {
  ChangeDetectionStrategy,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { Observable, Subject, takeUntil } from 'rxjs';
import { CirculatingTask } from '../../models';
import { TaskService } from '../../services';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './circulating.page.html',
  styleUrls: ['./circulating.page.scss'],
})
export class CirculatingPage implements OnInit, OnDestroy {
  tasks$: Observable<CirculatingTask[]>;
  private destroy$ = new Subject<boolean>();

  constructor(private TaskService: TaskService) {}

  ngOnInit(): void {
    this.tasks$ = this.TaskService.getTasks().pipe(takeUntil(this.destroy$));
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}
