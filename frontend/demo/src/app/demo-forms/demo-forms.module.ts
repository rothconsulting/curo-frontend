import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { StartFormComponent } from './start-form/start-form.component';

@NgModule({
  declarations: [StartFormComponent],
  imports: [CommonModule, ReactiveFormsModule, MatCardModule, MatInputModule]
})
export class DemoFormsModule {}
