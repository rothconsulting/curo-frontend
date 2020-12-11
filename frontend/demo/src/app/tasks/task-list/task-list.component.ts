import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { Task, TaskService } from '@umb-ag/curo-camunda';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-task-list',
  templateUrl: './task-list.component.html',
  styleUrls: ['./task-list.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TaskListComponent implements OnInit {
  tasks$: Observable<Task[]> | undefined;

  constructor(private taskService: TaskService) {}

  ngOnInit(): void {
    this.tasks$ = this.taskService.getTasks();
  }
}
