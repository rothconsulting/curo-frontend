import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  ViewChild
} from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort, Sort } from '@angular/material/sort';
import { ActivatedRoute } from '@angular/router';
import { Task, TaskService } from '@umb-ag/curo-core';
import { combineLatest, Observable, of, Subject } from 'rxjs';
import { catchError, map, startWith, switchMap, tap } from 'rxjs/operators';

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

  private reloadSubject = new Subject();

  constructor(
    private taskService: TaskService,
    private activatedRoute: ActivatedRoute
  ) {}

  ngAfterViewInit(): void {
    this.dataSource$ = combineLatest([
      this.sort.sortChange.pipe(startWith({} as Sort)),
      this.paginator.page.pipe(startWith(this.paginator)),
      this.activatedRoute.params.pipe(map((params) => params.filterId)),
      this.reloadSubject.asObservable().pipe(startWith(true))
    ]).pipe(
      tap(() => (this.isLoading = true)),
      switchMap(([sort, page, filterId, _]) => {
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

  get displayedColumns(): string[] {
    return this.columns?.map((column) => column.name) as any;
  }

  private isOrderedByTaskProperty(sortBy: string): boolean | undefined {
    return this.columns?.some(
      (column) => column.name === sortBy && column.isAttribute
    );
  }
}
