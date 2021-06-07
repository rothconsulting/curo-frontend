import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatCardModule } from '@angular/material/card';
import { MatListModule } from '@angular/material/list';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { TaskService } from '@umb-ag/curo-core';
import { of } from 'rxjs';
import { TaskListComponent } from './task-list.component';

describe('TaskListComponent', () => {
  let component: TaskListComponent;
  let fixture: ComponentFixture<TaskListComponent>;
  let taskService: TaskService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        NoopAnimationsModule,
        HttpClientTestingModule,
        RouterTestingModule,
        MatCardModule,
        MatListModule,
        MatPaginatorModule,
        MatSortModule,
        MatTableModule
      ],
      declarations: [TaskListComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            params: of({
              filterId: '9f2860a2-6637-11eb-b2aa-0a58ac80026c'
            })
          }
        }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TaskListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    taskService = TestBed.inject(TaskService);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should request tasklist', () => {
    spyOn(taskService, 'queryTasks').and.returnValue(
      of({ name: 'TestTaskList', total: 10, items: [] })
    );

    component.dataSource$?.subscribe();

    expect(taskService.queryTasks).toHaveBeenCalledWith(
      '9f2860a2-6637-11eb-b2aa-0a58ac80026c',
      {
        sorting: undefined
      },
      {
        includeFilter: true,
        offset: 0,
        maxResult: 10
      }
    );
  });

  it('should assign a user', () => {
    spyOn(taskService, 'assignTask').and.callThrough();

    component.assign('Tony', { id: 'test' });

    expect(taskService.assignTask).toHaveBeenCalledWith('test', 'Tony');
  });

  it('should return displayedColumns', () => {
    component.columns = [{ name: 'colA' }, { name: 'colB' }, { name: 'colC' }];

    expect(component.displayedColumns).toEqual(['colA', 'colB', 'colC']);
  });

  describe('getElementValue', () => {
    it('should get element attribute value', () => {
      expect(
        component.getElementValue(
          { test: 'testValue' },
          { isAttribute: true, name: 'test' }
        )
      ).toBe('testValue');
    });

    it('should get element variable value', () => {
      expect(
        component.getElementValue(
          { variables: { test: 'testValue' } },
          { name: 'test' }
        )
      ).toBe('testValue');
    });
  });
});
