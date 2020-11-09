import { HttpClient, HttpParams } from '@angular/common/http';
import { Inject, Injectable, Optional } from '@angular/core';
import { Observable } from 'rxjs';
import { ActivityStatisticsResult } from '../activity-statistics-result';
import { CAMUNDA_BASE_PATH } from '../camunda-base-path';
import { CountResult } from '../count-result';
import { Form } from '../form';
import { ProcessDefinition } from '../process-definition';
import { ProcessDefinitionDiagram } from '../process-definition-diagram';
import { ProcessDefinitionQuery } from '../process-definition-query';
import { ProcessDefinitionStatisticsResult } from '../process-definition-statistics-result';
import { ProcessDefinitionSuspensionState } from '../process-definition-suspension-state';
import { ProcessInstanceWithVariables } from '../process-instance-with-variables';
import { RestartProcessInstance } from '../restart-process-instance';
import { StartProcessInstance } from '../start-process-instance';
import { Variables } from '../variables';

@Injectable({
  providedIn: 'root'
})
export class ProcessDefinitionService {
  basePath: string;

  constructor(
    @Optional() @Inject(CAMUNDA_BASE_PATH) camundaBasePath: string,
    private httpClient: HttpClient
  ) {
    this.basePath = camundaBasePath || '';
  }

  getProcessDefinitions(
    query?: ProcessDefinitionQuery,
    maxResult?: number,
    firstResult?: number
  ): Observable<ProcessDefinition[]> {
    let params = this.createPagingQueryParams(maxResult, firstResult);

    if (query) {
      Object.entries(query).forEach(
        ([key, value]) => (params = params.append(key, `${value}`))
      );
    }

    return this.httpClient.get<ProcessDefinition[]>(
      `${this.basePath}/process-definition`,
      {
        params
      }
    );
  }

  getProcessDefinitionsCount(
    query?: ProcessDefinitionQuery
  ): Observable<CountResult> {
    let params = new HttpParams();

    if (query) {
      Object.entries(query).forEach(
        ([key, value]) => (params = params.append(key, `${value}`))
      );
    }
    return this.httpClient.get<CountResult>(
      `${this.basePath}/process-definition/count`,
      {
        params
      }
    );
  }

  deleteProcessDefinitionByKey(
    key: string,
    cascade?: boolean,
    skipCustomListeners?: boolean,
    skipIoMappings?: boolean
  ): Observable<void> {
    let params = new HttpParams();
    if (cascade !== undefined) {
      params = params.append('cascade', `${cascade}`);
    }
    if (skipCustomListeners !== undefined) {
      params = params.append('skipCustomListeners', `${skipCustomListeners}`);
    }
    if (skipCustomListeners !== undefined) {
      params = params.append('skipIoMappings', `${skipIoMappings}`);
    }

    return this.httpClient.delete<void>(
      `${this.basePath}/process-definition/key/${key}`,
      {
        params
      }
    );
  }

  getProcessDefinitionByKey(key: string): Observable<ProcessDefinition> {
    return this.httpClient.get<ProcessDefinition>(
      `${this.basePath}/process-definition/key/${key}`
    );
  }

  getDiagramByKey(key: string): Observable<string> {
    return this.httpClient.get<string>(
      `${this.basePath}/process-definition/key/${key}/diagram`
    );
  }

  getStartFormVariablesByKey(key: string): Observable<Variables> {
    return this.httpClient.get<Variables>(
      `${this.basePath}/process-definition/key/${key}/form-variables`
    );
  }

  updateHistoryTimeToLiveByKey(
    key: string,
    historyTimeToLive: number
  ): Observable<void> {
    return this.httpClient.put<void>(
      `${this.basePath}/process-definition/key/${key}/history-time-to-live`,
      { historyTimeToLive }
    );
  }

  startByKey(
    key: string,
    data?: StartProcessInstance
  ): Observable<ProcessInstanceWithVariables> {
    return this.httpClient.post<ProcessInstanceWithVariables>(
      `${this.basePath}/process-definition/key/${key}/start`,
      data
    );
  }

  getStartFormByKey(key: string): Observable<Form> {
    return this.httpClient.get<Form>(
      `${this.basePath}/process-definition/key/${key}/startForm`
    );
  }

  getStatisticsByKey(
    key: string,
    filter?: {
      failedJobs?: boolean;
      incidents?: boolean;
      incidentsForType?: string;
    }
  ): Observable<ActivityStatisticsResult> {
    let params = new HttpParams();
    if (filter?.failedJobs !== undefined) {
      params = params.append('failedJobs', `${filter.failedJobs}`);
    }
    if (filter?.incidents !== undefined) {
      params = params.append('incidents', `${filter.incidents}`);
    }
    if (filter?.incidentsForType !== undefined) {
      params = params.append('incidentsForType', filter.incidentsForType);
    }

    return this.httpClient.get<ActivityStatisticsResult>(
      `${this.basePath}/process-definition/key/${key}/statistics`,
      { params }
    );
  }

  suspendByKey(
    key: string,
    state: ProcessDefinitionSuspensionState
  ): Observable<void> {
    return this.httpClient.put<void>(
      `${this.basePath}/process-definition/key/${key}/suspended`,
      state
    );
  }

  getXmlByKey(key: string): Observable<ProcessDefinitionDiagram> {
    return this.httpClient.get<ProcessDefinitionDiagram>(
      `${this.basePath}/process-definition/key/${key}/xml`
    );
  }

  getStatistics(filter?: {
    failedJobs?: boolean;
    incidents?: boolean;
    incidentsForType?: string;
    rootIncidents?: boolean;
  }): Observable<ProcessDefinitionStatisticsResult[]> {
    let params = new HttpParams();
    if (filter?.failedJobs !== undefined) {
      params = params.append('failedJobs', `${filter.failedJobs}`);
    }
    if (filter?.incidents !== undefined) {
      params = params.append('incidents', `${filter.incidents}`);
    }
    if (filter?.incidentsForType !== undefined) {
      params = params.append('incidentsForType', filter.incidentsForType);
    }
    if (filter?.rootIncidents !== undefined) {
      params = params.append('rootIncidents', `${filter.rootIncidents}`);
    }

    return this.httpClient.get<ProcessDefinitionStatisticsResult[]>(
      `${this.basePath}/process-definition/statistics`,
      { params }
    );
  }

  suspend(state: ProcessDefinitionSuspensionState): Observable<void> {
    return this.httpClient.put<void>(
      `${this.basePath}/process-definition/suspended`,
      state
    );
  }

  deleteProcessDefinitionById(
    id: string,
    cascade?: boolean,
    skipCustomListeners?: boolean,
    skipIoMappings?: boolean
  ): Observable<void> {
    let params = new HttpParams();
    if (cascade !== undefined) {
      params = params.append('cascade', `${cascade}`);
    }
    if (skipCustomListeners !== undefined) {
      params = params.append('skipCustomListeners', `${skipCustomListeners}`);
    }
    if (skipCustomListeners !== undefined) {
      params = params.append('skipIoMappings', `${skipIoMappings}`);
    }

    return this.httpClient.delete<void>(
      `${this.basePath}/process-definition/${id}`,
      {
        params
      }
    );
  }

  getProcessDefinitionById(id: string): Observable<ProcessDefinition> {
    return this.httpClient.get<ProcessDefinition>(
      `${this.basePath}/process-definition/${id}`
    );
  }

  getDiagramById(id: string): Observable<string> {
    return this.httpClient.get<string>(
      `${this.basePath}/process-definition/${id}/diagram`
    );
  }

  getStartFormVariablesById(id: string): Observable<Variables> {
    return this.httpClient.get<Variables>(
      `${this.basePath}/process-definition/${id}/form-variables`
    );
  }

  updateHistoryTimeToLiveById(
    id: string,
    historyTimeToLive: number
  ): Observable<void> {
    return this.httpClient.put<void>(
      `${this.basePath}/process-definition/${id}/history-time-to-live`,
      { historyTimeToLive }
    );
  }

  restartProcessInstanceById(
    id: string,
    data: RestartProcessInstance
  ): Observable<void> {
    return this.httpClient.post<void>(
      `${this.basePath}/process-definition/${id}/restart`,
      data
    );
  }
  restartProcessInstanceAsyncById(
    id: string,
    data: RestartProcessInstance
  ): Observable<void> {
    return this.httpClient.post<void>(
      `${this.basePath}/process-definition/${id}/restart-async`,
      data
    );
  }

  startById(
    id: string,
    data?: StartProcessInstance
  ): Observable<ProcessInstanceWithVariables> {
    return this.httpClient.post<ProcessInstanceWithVariables>(
      `${this.basePath}/process-definition/${id}/start`,
      data
    );
  }

  getStartFormById(id: string): Observable<Form> {
    return this.httpClient.get<Form>(
      `${this.basePath}/process-definition/${id}/startForm`
    );
  }

  getStatisticsById(
    id: string,
    filter?: {
      failedJobs?: boolean;
      incidents?: boolean;
      incidentsForType?: string;
    }
  ): Observable<ActivityStatisticsResult> {
    let params = new HttpParams();
    if (filter?.failedJobs !== undefined) {
      params = params.append('failedJobs', `${filter.failedJobs}`);
    }
    if (filter?.incidents !== undefined) {
      params = params.append('incidents', `${filter.incidents}`);
    }
    if (filter?.incidentsForType !== undefined) {
      params = params.append('incidentsForType', filter.incidentsForType);
    }

    return this.httpClient.get<ActivityStatisticsResult>(
      `${this.basePath}/process-definition/${id}/statistics`,
      { params }
    );
  }

  suspendById(
    id: string,
    state: ProcessDefinitionSuspensionState
  ): Observable<void> {
    return this.httpClient.put<void>(
      `${this.basePath}/process-definition/${id}/suspended`,
      state
    );
  }

  getXmlById(id: string): Observable<ProcessDefinitionDiagram> {
    return this.httpClient.get<ProcessDefinitionDiagram>(
      `${this.basePath}/process-definition/${id}/xml`
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
