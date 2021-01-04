import { HttpClient } from '@angular/common/http';
import { Inject, Injectable, Optional } from '@angular/core';
import { Params } from '@angular/router';
import { Observable } from 'rxjs';
import { CURO_BASE_PATH } from '../curo-base-path';
import { Task } from '../task/task';

@Injectable({
  providedIn: 'root'
})
export class ProcessService {
  basePath: string;

  constructor(
    @Optional() @Inject(CURO_BASE_PATH) curoBasePath: string,
    private httpClient: HttpClient
  ) {
    this.basePath = curoBasePath || '';
  }

  startProcess(
    processDefinitionKey: string,
    variables?: any,
    businessKey?: string,
    params?: {
      returnVariables?: boolean;
    }
  ): Observable<Task | void> {
    return this.httpClient.post<Task | void>(
      `${this.basePath}/process-instances`,
      { processDefinitionKey, variables, businessKey },
      { params: params as Params }
    );
  }
}
