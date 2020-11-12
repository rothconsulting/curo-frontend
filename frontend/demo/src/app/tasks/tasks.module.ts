import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatListModule } from '@angular/material/list';
import { TaskListComponent } from './task-list/task-list.component';
import { TaskComponent } from './task/task.component';
import { TasksRoutingModule } from './tasks-routing.module';

@NgModule({
  declarations: [TaskListComponent, TaskComponent],
  imports: [CommonModule, MatListModule, MatCardModule, TasksRoutingModule]
})
export class TasksModule {}
