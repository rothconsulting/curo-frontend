import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { RouterModule } from '@angular/router';
import { MenuComponent } from './menu/menu.component';
import { TaskBarComponent } from './task-bar/task-bar.component';

@NgModule({
  declarations: [MenuComponent, TaskBarComponent],
  imports: [
    CommonModule,
    RouterModule,
    MatButtonModule,
    MatIconModule,
    MatListModule
  ],
  exports: [MenuComponent, TaskBarComponent]
})
export class SharedModule {}
