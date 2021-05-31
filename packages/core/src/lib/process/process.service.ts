import { HttpClient } from '@angular/common/http';
import { Inject, Injectable, Optional } from '@angular/core';
import { Params } from '@angular/router';
import { Observable } from 'rxjs';
import { CURO_BASE_PATH } from '../curo-base-path';
import { FlowToNext } from '../task/flow-to-next';
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

  /**
   * Start a new process instance.
   */
  startProcess(
    processDefinitionKey: string,
    variables?: any,
    businessKey?: string,
    params?: {
      returnVariables?: boolean;
      flowToNext?: boolean;
      flowToNextIgnoreAssignee?: boolean;
      flowToNextTimeOut?: number;
    }
  ): Observable<FlowToNext & Task> {
    return this.httpClient.post<Task & FlowToNext>(
      `${this.basePath}/process-instances`,
      { processDefinitionKey, variables, businessKey },
      { params: params as Params }
    );
  }

  /**
   * Start a new process instance with files.
   */
  startProcessWithFiles(
    processDefinitionKey: string,
    variables?: any,
    files?: { [name: string]: any },
    businessKey?: string,
    params?: {
      returnVariables?: boolean;
      flowToNext?: boolean;
      flowToNextIgnoreAssignee?: boolean;
      flowToNextTimeOut?: number;
    }
  ): Observable<FlowToNext & Task> {
    const formData = new FormData();
    formData.append(
      'processStartData',
      JSON.stringify({ processDefinitionKey, variables, businessKey })
    );

    Object.keys(files || {}).forEach((name) =>
      formData.append(name, files![name])
    );

    return this.httpClient.post<Task & FlowToNext>(
      `${this.basePath}/process-instances`,
      formData,
      { params: params as Params }
    );
  }

  /**
   * Query for next user task of process instance.
   */
  nextTask(
    id: string,
    flowToNextIgnoreAssignee?: boolean
  ): Observable<FlowToNext> {
    return this.httpClient.get<FlowToNext>(
      `${this.basePath}/process-instances/${id}/next`,
      { params: { flowToNextIgnoreAssignee } as Params }
    );
  }
}
