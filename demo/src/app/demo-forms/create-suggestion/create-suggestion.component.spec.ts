import { HttpClientTestingModule } from '@angular/common/http/testing';
import {
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick
} from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { TaskService } from '@umb-ag/curo-core';
import { of, throwError } from 'rxjs';
import { SharedModule } from '../../shared/shared.module';
import { CreateSuggestionComponent } from './create-suggestion.component';

describe('CreateSuggestionComponent', () => {
  let component: CreateSuggestionComponent;
  let fixture: ComponentFixture<CreateSuggestionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        NoopAnimationsModule,
        RouterTestingModule,
        HttpClientTestingModule,
        ReactiveFormsModule,
        MatCardModule,
        MatInputModule,
        MatSnackBarModule,
        SharedModule
      ],
      declarations: [CreateSuggestionComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateSuggestionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnInit', () => {
    it('should get task variables', () => {
      const mockData = {
        title: 'demo title',
        category: 'demo category',
        description: 'demo description',
        url: 'https://github.com'
      };

      const service = TestBed.inject(TaskService);
      spyOn(service, 'getTask').and.returnValue(of({ variables: mockData }));

      component.ngOnInit();

      expect(component.form.value).toEqual(mockData);
    });

    it('should open a snack bar on error', () => {
      const taskService = TestBed.inject(TaskService);
      const matSnackBar = TestBed.inject(MatSnackBar);
      spyOn(taskService, 'getTask').and.throwError('error');
      spyOn(matSnackBar, 'open').and.callThrough();

      component.ngOnInit();

      expect(matSnackBar.open).toHaveBeenCalled();
    });
  });

  it('should open a snack bar when variables are saved', fakeAsync(() => {
    const taskService = TestBed.inject(TaskService);
    const matSnackBar = TestBed.inject(MatSnackBar);
    spyOn(matSnackBar, 'open').and.callThrough();
    spyOn(taskService, 'saveVariables').and.returnValue(of(undefined));

    const mockData = {
      title: 'demo title',
      category: 'demo category',
      description: 'demo description',
      url: 'https://github.com'
    };

    component.registerValueChanges();

    component.form.patchValue(mockData);

    tick(2000);

    expect(matSnackBar.open).toHaveBeenCalledWith(
      'Variables saved successfully',
      'Done',
      {
        duration: 2000
      }
    );
  }));

  it('should open a snack bar when variables are not saved', fakeAsync(() => {
    const taskService = TestBed.inject(TaskService);
    const matSnackBar = TestBed.inject(MatSnackBar);
    spyOn(matSnackBar, 'open').and.callThrough();
    spyOn(taskService, 'saveVariables').and.returnValue(
      throwError({ error: 'error' })
    );

    const mockData = {
      title: 'demo title',
      category: 'demo category',
      description: 'demo description',
      url: 'https://github.com'
    };

    component.registerValueChanges();

    component.form.patchValue(mockData);

    tick(2000);

    expect(matSnackBar.open).toHaveBeenCalledWith(
      'Variables could not be saved',
      'Done',
      {
        duration: 2000
      }
    );
  }));

  describe('assign', () => {
    it('should show a snack bar when assigning a task was successful', () => {
      const taskService = TestBed.inject(TaskService);
      const matSnackBar = TestBed.inject(MatSnackBar);
      spyOn(taskService, 'assignTask').and.returnValue(of(undefined));
      spyOn(matSnackBar, 'open').and.callThrough();

      component.assign();

      expect(taskService.assignTask).toHaveBeenCalledWith(
        undefined as any,
        'demo'
      );
      expect(matSnackBar.open).toHaveBeenCalledWith(
        'Task assigned successfully',
        'Done',
        {
          duration: 2000
        }
      );
    });

    it('should show a snack bar when assigning a task was not successful', () => {
      const taskService = TestBed.inject(TaskService);
      const matSnackBar = TestBed.inject(MatSnackBar);
      spyOn(taskService, 'assignTask').and.returnValue(
        throwError({ error: 'error' })
      );
      spyOn(matSnackBar, 'open').and.callThrough();

      component.assign();

      expect(taskService.assignTask).toHaveBeenCalledWith(
        undefined as any,
        'demo'
      );
      expect(matSnackBar.open).toHaveBeenCalledWith(
        'Task could not be assigned',
        'Done',
        {
          duration: 2000
        }
      );
    });
  });

  describe('complete', () => {
    it('should call complete task when form value is valid', () => {
      const taskService = TestBed.inject(TaskService);
      spyOn(taskService, 'completeTask').and.returnValue(of({}));
      const router = TestBed.inject(Router);
      spyOn(router, 'navigate').and.returnValue(
        new Promise((resolve) => resolve(true))
      );
      const activatedRoute = TestBed.inject(ActivatedRoute);

      component.form.patchValue({
        title: 'demo title',
        description: 'demo description'
      });

      component.complete();

      expect(taskService.completeTask).toHaveBeenCalled();
      expect(router.navigate).toHaveBeenCalledWith(['..', ''], {
        relativeTo: activatedRoute
      });
    });
    it('should call complete task and flow to next when form value is valid', () => {
      const taskService = TestBed.inject(TaskService);
      spyOn(taskService, 'completeTask').and.returnValue(
        of({ flowToNext: ['next-task'] })
      );
      const router = TestBed.inject(Router);
      spyOn(router, 'navigate').and.returnValue(
        new Promise((resolve) => resolve(true))
      );
      const activatedRoute = TestBed.inject(ActivatedRoute);

      component.form.patchValue({
        title: 'demo title',
        description: 'demo description'
      });

      component.complete();

      expect(taskService.completeTask).toHaveBeenCalled();
      expect(router.navigate).toHaveBeenCalledWith(['..', 'next-task'], {
        relativeTo: activatedRoute
      });
    });

    it('should not call complete task when form value is invalid', () => {
      const taskService = TestBed.inject(TaskService);
      spyOn(taskService, 'completeTask').and.callThrough();

      component.complete();

      expect(taskService.completeTask).not.toHaveBeenCalled();
    });
  });
});
