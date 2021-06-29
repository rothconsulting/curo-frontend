import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  ViewChild
} from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort, Sort } from '@angular/material/sort';
import { ActivatedRoute } from '@angular/router';
import { Task, TaskService } from '@umb-ag/curo-core';
import { combineLatest, Observable, of, Subject } from 'rxjs';
import {
  catchError,
  debounceTime,
  distinctUntilChanged,
  map,
  startWith,
  switchMap,
  tap
} from 'rxjs/operators';

@Component({
  selector: 'app-task-list',
  templateUrl: './task-list.component.html',
  styleUrls: ['./task-list.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TaskListComponent implements AfterViewInit {
  @ViewChild(MatPaginator, { static: true })
  paginator!: MatPaginator;

  @ViewChild(MatSort, { static: true })
  sort!: MatSort;

  isLoading = false;
  title?: string;
  total?: number;

  dataSource$?: Observable<Task[]>;
  columns?: any[];
  filterProperties?: any[];

  filterForm: FormGroup;

  private reloadSubject = new Subject();

  constructor(
    private taskService: TaskService,
    private activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {
    this.filterForm = fb.group({});
  }

  ngAfterViewInit(): void {
    const filter$ = this.filterForm.valueChanges.pipe(
      startWith({}),
      debounceTime(500),
      distinctUntilChanged((prev, curr) =>
        Object.keys(curr).every(
          (value) => prev[value]?.value === curr[value]?.value
        )
      ),
      map((value) =>
        Object.keys(value)
          .map((name) => {
            return { name, ...value[name] };
          })
          .filter((value) => value.value !== null && value.value !== '')
          .reduce((prev, current) => {
            const filter = { ...prev };
            if (current.isAttribute) {
              filter[current.name] = current.value;
            } else {
              const { isAttribute, ...rest } = current;
              filter.processVariables = [
                ...(filter.processVariables || []),
                rest
              ];
            }
            return filter;
          }, {})
      )
    );

    this.dataSource$ = combineLatest([
      this.sort.sortChange.pipe(startWith({} as Sort)),
      this.paginator.page.pipe(startWith(this.paginator)),
      this.activatedRoute.params.pipe(map((params) => params.filterId)),
      this.reloadSubject.asObservable().pipe(startWith(true)),
      filter$
    ]).pipe(
      tap(() => (this.isLoading = true)),
      switchMap(([sort, page, filterId, _, filter]) => {
        let sorting;
        if (sort.direction) {
          const sortBy = sort.active;
          const sortOrder = sort.direction;
          const isTaskProperty = this.isOrderedByTaskProperty(sortBy);

          const parameters = isTaskProperty
            ? undefined
            : { variable: sortBy, type: 'String' };

          sorting = [
            {
              sortBy: isTaskProperty ? sortBy : 'processVariable',
              sortOrder,
              parameters
            }
          ] as any;
        }

        return this.taskService
          .queryTasks(
            filterId,
            {
              ...filter,
              sorting
            },
            {
              includeFilter: true,
              offset: page.pageIndex * page.pageSize || 0,
              maxResult: page.pageSize
            }
          )
          .pipe(
            catchError(() => of({ items: [] } as any)),
            tap((taskList) => {
              this.isLoading = false;
              this.title = taskList.name;
              this.total = taskList.total;
              this.columns = taskList.properties?.variables;
              this.filterProperties = taskList.filterProperties;
              this.filterProperties?.forEach((filterProperty) =>
                this.addFilterFormControl(filterProperty)
              );
            }),
            map((taskList) => taskList.items)
          );
      })
    );
  }

  getElementValue(element: any, column: any): any {
    return column.isAttribute
      ? element[column.name]
      : element.variables[column.name];
  }

  assign(assignee: string, task: any) {
    this.taskService
      .assignTask(task.id, assignee)
      .subscribe(() => this.reloadSubject.next());
  }

  filterPropertyTrackByFn(_: number, filterProperty: any) {
    return filterProperty.variable;
  }

  get displayedColumns(): string[] {
    return this.columns?.map((column) => column.name) as any;
  }

  private isOrderedByTaskProperty(sortBy: string): boolean | undefined {
    return this.columns?.some(
      (column) => column.name === sortBy && column.isAttribute
    );
  }

  private addFilterFormControl(filterProperty: any) {
    const group = this.fb.group({
      isAttribute: [],
      operator: [],
      value: []
    });

    group.patchValue(filterProperty);

    this.filterForm.addControl(filterProperty.variable, group, {
      emitEvent: false
    });
  }
}
