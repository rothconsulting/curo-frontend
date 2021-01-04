import {
  ChangeDetectionStrategy,
  Component,
  OnDestroy,
  OnInit
} from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute } from '@angular/router';
import { TaskService } from '@umb-ag/curo-core';
import { Observable, of, Subscription } from 'rxjs';
import {
  catchError,
  debounceTime,
  map,
  switchMap,
  take,
  withLatestFrom
} from 'rxjs/operators';

@Component({
  selector: 'app-create-suggestion',
  templateUrl: './create-suggestion.component.html',
  styleUrls: ['./create-suggestion.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CreateSuggestionComponent implements OnInit, OnDestroy {
  form: FormGroup;
  private valueChangesSubscription?: Subscription;

  constructor(
    private activatedRoute: ActivatedRoute,
    private taskService: TaskService,
    private matSnackBar: MatSnackBar,
    fb: FormBuilder
  ) {
    this.form = fb.group({
      title: [null, Validators.required],
      category: [],
      description: [null, Validators.required],
      url: []
    });
  }

  ngOnInit(): void {
    const taskId$ = this.activatedRoute.params.pipe(
      map((params) => params.taskId)
    );

    taskId$
      .pipe(switchMap((taskId) => this.taskService.assignTask(taskId, 'demo')))
      .subscribe();

    taskId$
      .pipe(
        switchMap((taskId) =>
          this.taskService.getTask(taskId, {
            attributes: ['variables']
          })
        ),
        map((task) => task.variables),
        take(1)
      )
      .subscribe(
        (variables: any) => {
          this.form.patchValue(variables);
          this.registerValueChanges(taskId$);
        },
        () => {
          this.registerValueChanges(taskId$);
          this.matSnackBar.open('Variables could not be loaded', 'Done', {
            duration: 2000
          });
        }
      );
  }

  ngOnDestroy(): void {
    this.valueChangesSubscription?.unsubscribe();
  }

  registerValueChanges(taskId$: Observable<string>): void {
    this.valueChangesSubscription = this.form.valueChanges
      .pipe(
        debounceTime(1000),
        withLatestFrom(taskId$),
        switchMap(([variables, taskId]) =>
          this.taskService
            .saveVariables(taskId, variables)
            .pipe(catchError((error) => of(error)))
        )
      )
      .subscribe((data) => {
        if (data?.error) {
          this.matSnackBar.open('Variables could not be saved', 'Done', {
            duration: 2000
          });
        } else {
          this.matSnackBar.open('Variables saved successfully', 'Done', {
            duration: 2000
          });
        }
      });
  }
}
