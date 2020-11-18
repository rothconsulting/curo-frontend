import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatListModule } from '@angular/material/list';
import { CuroCoreModule } from '@umb-ag/curo-core';
import { TaskListComponent } from './task-list/task-list.component';
import { TaskComponent } from './task/task.component';
import { TasksRoutingModule } from './tasks-routing.module';

@NgModule({
  declarations: [TaskListComponent, TaskComponent],
  imports: [
    CommonModule,
    MatListModule,
    MatCardModule,
    TasksRoutingModule,
    CuroCoreModule
  ]
})
export class TasksModule {}
