import { HttpClient, HttpParams } from '@angular/common/http';
import { Inject, Injectable, Optional } from '@angular/core';
import { Observable } from 'rxjs';
import { ActivityInstance } from '../activity-instance';
import { Batch } from '../batch';
import { CAMUNDA_BASE_PATH } from '../camunda-base-path';
import { CountResult } from '../count-result';
import { DeleteProcessInstances } from '../delete-process-instances';
import { ProcessInstance } from '../process-instance';
import { ProcessInstanceModification } from '../process-instance-modification';
import { ProcessInstanceQuery } from '../process-instance-query';
import { ProcessInstanceSuspensionState } from '../process-instance-suspension-state';
import { ProcessInstanceSuspensionStateAsync } from '../process-instance-suspension-state-async';
import { SetJobRetriesByProcess } from '../set-job-retries-by-process';
import { SetVariablesAsync } from '../set-variables-async';
import { VariableValue } from '../variable-value';
import { Variables } from '../variables';

@Injectable({
  providedIn: 'root'
})
export class ProcessInstanceService {
  basePath: string;

  constructor(
    @Optional() @Inject(CAMUNDA_BASE_PATH) camundaBasePath: string,
    private httpClient: HttpClient
  ) {
    this.basePath = camundaBasePath || '';
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/process-instance/get-query/
   */
  getProcessInstances(
    maxResult?: number,
    firstResult?: number
  ): Observable<ProcessInstance[]> {
    const params = this.createPagingQueryParams(maxResult, firstResult);

    return this.httpClient.get<ProcessInstance[]>(
      `${this.basePath}/process-instance`,
      { params }
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/process-instance/post-query/
   */
  queryProcessInstances(
    query?: ProcessInstanceQuery,
    maxResult?: number,
    firstResult?: number
  ): Observable<ProcessInstance[]> {
    const params = this.createPagingQueryParams(maxResult, firstResult);

    return this.httpClient.post<ProcessInstance[]>(
      `${this.basePath}/process-instance`,
      query,
      { params }
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/process-instance/get-query-count/
   */
  getProcessInstancesCount(): Observable<CountResult> {
    return this.httpClient.get<CountResult>(
      `${this.basePath}/process-instance/count`
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/process-instance/post-query-count/
   */
  queryProcessInstancesCount(
    query?: ProcessInstanceQuery
  ): Observable<CountResult> {
    return this.httpClient.post<CountResult>(
      `${this.basePath}/process-instance/count`,
      query
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/process-instance/post-delete/
   */
  deleteProcessInstances(data: DeleteProcessInstances): Observable<Batch> {
    return this.httpClient.post<Batch>(
      `${this.basePath}/process-instance/delete`,
      data
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/process-instance/post-delete-historic-query-based/
   */
  deleteProcessInstancesByHistoricQuery(
    data: DeleteProcessInstances
  ): Observable<Batch> {
    return this.httpClient.post<Batch>(
      `${this.basePath}/process-instance/delete-historic-query-based`,
      data
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/process-instance/post-set-job-retries/
   */
  setJobRetries(data: SetJobRetriesByProcess): Observable<Batch> {
    return this.httpClient.post<Batch>(
      `${this.basePath}/process-instance/job-retries`,
      data
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/process-instance/post-set-job-retries-historic-query-based/
   */
  setJobRetriesByHistoricQuery(
    data: SetJobRetriesByProcess
  ): Observable<Batch> {
    return this.httpClient.post<Batch>(
      `${this.basePath}/process-instance/job-retries-historic-query-based`,
      data
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/process-instance/put-activate-suspend-in-group/
   */
  suspend(state: ProcessInstanceSuspensionState): Observable<void> {
    return this.httpClient.put<void>(
      `${this.basePath}/process-instance/suspended`,
      state
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/process-instance/post-activate-suspend-in-batch/
   */
  suspendAsync(state: ProcessInstanceSuspensionStateAsync): Observable<Batch> {
    return this.httpClient.post<Batch>(
      `${this.basePath}/process-instance/suspended-async`,
      state
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/process-instance/post-set-variables-async/
   */
  setVariablesAsync(variables: SetVariablesAsync): Observable<Batch> {
    return this.httpClient.post<Batch>(
      `${this.basePath}/process-instance/variables-async`,
      variables
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/process-instance/delete/
   */
  deleteProcessInstance(
    id: string,
    options?: {
      skipCustomListeners?: boolean;
      skipIoMappings?: boolean;
      skipSubprocesses?: boolean;
      failIfNotExists?: boolean;
    }
  ): Observable<void> {
    let params = new HttpParams();
    if (options?.skipCustomListeners !== undefined) {
      params = params.append(
        'skipCustomListeners',
        `${options.skipCustomListeners}`
      );
    }
    if (options?.skipIoMappings !== undefined) {
      params = params.append('skipIoMappings', `${options.skipIoMappings}`);
    }
    if (options?.skipSubprocesses !== undefined) {
      params = params.append('skipSubprocesses', `${options.skipSubprocesses}`);
    }
    if (options?.failIfNotExists !== undefined) {
      params = params.append('failIfNotExists', `${options.failIfNotExists}`);
    }

    return this.httpClient.delete<void>(
      `${this.basePath}/process-instance/${id}`,
      { params }
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/process-instance/get/
   */
  getProcessInstance(id: string): Observable<ProcessInstance> {
    return this.httpClient.get<ProcessInstance>(
      `${this.basePath}/process-instance/${id}`
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/process-instance/get-activity-instances/
   */
  getActivityInstance(id: string): Observable<ActivityInstance> {
    return this.httpClient.get<ActivityInstance>(
      `${this.basePath}/process-instance/${id}/activity-instances`
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/process-instance/post-modification/
   */
  modifyProcessInstance(
    id: string,
    data: ProcessInstanceModification
  ): Observable<void> {
    return this.httpClient.post<void>(
      `${this.basePath}/process-instance/${id}/modification`,
      data
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/process-instance/post-modification-async/
   */
  modifyProcessInstanceAsync(
    id: string,
    data: ProcessInstanceModification
  ): Observable<Batch> {
    return this.httpClient.post<Batch>(
      `${this.basePath}/process-instance/${id}/modification-async`,
      data
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/process-instance/put-activate-suspend-by-proc-def-id/
   */
  suspendProcessInstance(id: string, suspended: boolean): Observable<void> {
    return this.httpClient.put<void>(
      `${this.basePath}/process-instance/${id}/suspended`,
      { suspended }
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/process-instance/variables/get-variables/
   */
  getVariables(id: string): Observable<Variables> {
    return this.httpClient.get<Variables>(
      `${this.basePath}/process-instance/${id}/variables`
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/process-instance/variables/post-variables/
   */
  patchVariables(
    id: string,
    modifications?: Variables,
    deletions?: string[]
  ): Observable<Variables> {
    return this.httpClient.post<Variables>(
      `${this.basePath}/process-instance/${id}/variables`,
      { modifications, deletions }
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/process-instance/variables/delete-variable/
   */
  deleteVariable(id: string, variableName: string): Observable<void> {
    return this.httpClient.delete<void>(
      `${this.basePath}/process-instance/${id}/variables/${variableName}`
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/process-instance/variables/get-variable/
   */
  getVariable<T>(
    id: string,
    variableName: string
  ): Observable<VariableValue<T>> {
    return this.httpClient.get<VariableValue<T>>(
      `${this.basePath}/process-instance/${id}/variables/${variableName}`
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/process-instance/variables/put-variable/
   */
  setVariable(
    id: string,
    variableName: string,
    value: VariableValue<unknown>
  ): Observable<void> {
    return this.httpClient.put<void>(
      `${this.basePath}/process-instance/${id}/variables/${variableName}`,
      value
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/process-instance/variables/get-variable-binary/
   */
  getVariableData(id: string, variableName: string): Observable<string> {
    return this.httpClient.get<string>(
      `${this.basePath}/process-instance/${id}/variables/${variableName}/data`
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/process-instance/variables/post-variable-binary/
   */
  setVariableData(
    id: string,
    variableName: string,
    data: string,
    type: string
  ): Observable<void> {
    const formData = new FormData();
    formData.append('data', data);
    formData.append('valueType', type);

    return this.httpClient.post<void>(
      `${this.basePath}/process-instance/${id}/variables/${variableName}/data`,
      formData
    );
  }

  private createPagingQueryParams(
    maxResult?: number,
    firstResult?: number
  ): HttpParams {
    let params = new HttpParams();

    if (maxResult) {
      params = params.append('maxResult', `${maxResult}`);
    }

    if (firstResult) {
      params = params.append('firstResult', `${firstResult}`);
    }

    return params;
  }
}
