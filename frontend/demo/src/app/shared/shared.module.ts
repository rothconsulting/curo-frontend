import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { TaskBarComponent } from './task-bar/task-bar.component';

@NgModule({
  declarations: [TaskBarComponent],
  imports: [CommonModule, MatButtonModule, MatIconModule],
  exports: [TaskBarComponent]
})
export class SharedModule {}
