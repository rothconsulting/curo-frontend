import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { CreateSuggestionComponent } from './create-suggestion/create-suggestion.component';
import { ReviewSuggestionComponent } from './review-suggestion/review-suggestion.component';
import { StartFormComponent } from './start-form/start-form.component';

@NgModule({
  declarations: [
    StartFormComponent,
    CreateSuggestionComponent,
    ReviewSuggestionComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatInputModule,
    MatSnackBarModule
  ],
  exports: [CreateSuggestionComponent, ReviewSuggestionComponent]
})
export class DemoFormsModule {}
