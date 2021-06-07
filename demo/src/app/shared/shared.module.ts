import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { RouterModule } from '@angular/router';
import { AssignDialogComponent } from './assign-dialog/assign-dialog.component';
import { MenuComponent } from './menu/menu.component';
import { TaskBarComponent } from './task-bar/task-bar.component';

@NgModule({
  declarations: [AssignDialogComponent, MenuComponent, TaskBarComponent],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    MatAutocompleteModule,
    MatButtonModule,
    MatDialogModule,
    MatIconModule,
    MatInputModule,
    MatListModule
  ],
  exports: [AssignDialogComponent, MenuComponent, TaskBarComponent]
})
export class SharedModule {}
