import { HttpClient, HttpResponse } from '@angular/common/http';
import { Inject, Injectable, Optional } from '@angular/core';
import { Params } from '@angular/router';
import { Observable } from 'rxjs';
import { CURO_BASE_PATH } from '../curo-base-path';
import { FlowToNext } from './flow-to-next';
import { Task } from './task';
import { TaskList } from './task-list';
import { TaskQuery } from './task-query';

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
   * Load a list of tasks using a camunda filter.
   */
  getTasks(
    filterId: string,
    params?: {
      query?: string;
      attributes?: string[];
      variables?: string[];
      offset?: number;
      maxResult?: number;
      includeFilter?: boolean;
    }
  ): Observable<TaskList> {
    const newParams = { ...params, id: filterId };
    return this.httpClient.get<TaskList>(`${this.basePath}/tasks`, {
      params: newParams as Params
    });
  }

  /**
   * Load a list of tasks using a camunda filter with additional query options.
   */
  queryTasks(
    filterId: string,
    query?: TaskQuery,
    params?: {
      attributes?: string[];
      variables?: string[];
      offset?: number;
      maxResult?: number;
      includeFilter?: boolean;
    }
  ): Observable<TaskList> {
    const newParams = { ...params, id: filterId };
    return this.httpClient.post<TaskList>(`${this.basePath}/tasks`, query, {
      params: newParams as Params
    });
  }

  /**
   * Get task by id.
   */
  getTask(
    id: string,
    params?: {
      variables?: string[];
      attributes?: string[];
      historic?: boolean;
    }
  ): Observable<Task> {
    return this.httpClient.get<Task>(`${this.basePath}/tasks/${id}`, {
      params: params as Params
    });
  }

  /**
   * Set the assignee of a task.
   */
  assignTask(id: string, assignee?: string | null): Observable<void> {
    return this.httpClient.put<void>(`${this.basePath}/tasks/${id}/assignee`, {
      assignee
    });
  }

  /**
   * Completes a task.
   */
  completeTask(
    id: string,
    variables?: any,
    params?: {
      returnVariables?: boolean;
      flowToNext?: boolean;
      flowToNextIgnoreAssignee?: boolean;
      flowToNextTimeOut?: number;
    }
  ): Observable<Task & FlowToNext> {
    return this.httpClient.post<Task & FlowToNext>(
      `${this.basePath}/tasks/${id}/status`,
      variables,
      { params: params as Params }
    );
  }

  /**
   * Query for next user task.
   */
  nextTask(
    id: string,
    flowToNextIgnoreAssignee?: boolean
  ): Observable<FlowToNext> {
    return this.httpClient.get<FlowToNext>(
      `${this.basePath}/tasks/${id}/next`,
      { params: { flowToNextIgnoreAssignee } as Params }
    );
  }

  /**
   * Saves the variables of a task.
   */
  saveVariables(id: string, variables?: any): Observable<void> {
    return this.httpClient.patch<void>(
      `${this.basePath}/tasks/${id}/variables`,
      variables
    );
  }

  /**
   * Download the file of a variable.
   */
  getFile(id: string, variableName: string): Observable<HttpResponse<Blob>> {
    return this.httpClient.get(
      `${this.basePath}/tasks/${id}/file/${variableName}`,
      { observe: 'response', responseType: 'blob' }
    );
  }

  /**
   * Download file variables as zip.
   */
  getFilesZipped(
    id: string,
    variableNames: string[],
    name?: string,
    ignoreNotExistingFiles?: boolean
  ): Observable<HttpResponse<Blob>> {
    let params = { files: variableNames };
    if (name) {
      params = { ...params, name } as any;
    }
    if (ignoreNotExistingFiles !== undefined) {
      params = { ...params, ignoreNotExistingFiles } as any;
    }

    return this.httpClient.get(`${this.basePath}/tasks/${id}/zip-files`, {
      observe: 'response',
      responseType: 'blob',
      params
    });
  }
}
