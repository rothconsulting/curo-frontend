import { HttpClient } from '@angular/common/http';
import { Inject, Injectable, Optional } from '@angular/core';
import { Params } from '@angular/router';
import { Observable } from 'rxjs';
import { CURO_BASE_PATH } from '../curo-base-path';
import { Task } from './task';

@Injectable({
  providedIn: 'root'
})
export class TaskService {
  basePath: string;

  constructor(
    @Optional() @Inject(CURO_BASE_PATH) curoBasePath: string,
    private httpClient: HttpClient
  ) {
    this.basePath = curoBasePath || '';
  }

  /**
   * Get task by id.
   */
  getTask(
    id: string,
    params?: {
      variables?: string[];
      attributes?: string[];
    }
  ): Observable<Task> {
    return this.httpClient.get<Task>(`${this.basePath}/tasks/${id}`, {
      params: params as Params
    });
  }
}
