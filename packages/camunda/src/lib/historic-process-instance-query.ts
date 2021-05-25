import { VariableQueryParameter } from './variable-query-parameter';

export interface HistoricProcessInstanceQuery {
  processInstanceId: string;
  processInstanceIds: string[];
  processDefinitionId: string;
  processDefinitionKey: string;
  processDefinitionKeyIn: string[];
  processDefinitionName: string;
  processDefinitionNameLike: string;
  processDefinitionKeyNotIn: string[];
  processInstanceBusinessKey: string;
  processInstanceBusinessKeyLike: string;
  rootProcessInstances: boolean;
  finished: boolean;
  unfinished: boolean;
  withIncidents: boolean;
  withRootIncidents: boolean;
  incidentType: string;
  incidentStatus: 'open' | 'resolved';
  incidentMessage: string;
  incidentMessageLike: string;
  startedBefore: string;
  startedAfter: string;
  finishedBefore: string;
  finishedAfter: string;
  executedActivityAfter: string;
  executedActivityBefore: string;
  executedJobAfter: string;
  executedJobBefore: string;
  startedBy: string;
  superProcessInstanceId: string;
  subProcessInstanceId: string;
  superCaseInstanceId: string;
  subCaseInstanceId: string;
  caseInstanceId: string;
  tenantIdIn: string[];
  withoutTenantId: boolean;
  executedActivityIdIn: string[];
  activeActivityIdIn: string[];
  active: boolean;
  suspended: boolean;
  completed: boolean;
  externallyTerminated: boolean;
  internallyTerminated: boolean;
  variables: VariableQueryParameter[];
  variableNamesIgnoreCase: boolean;
  variableValuesIgnoreCase: boolean;
  orQueries: HistoricProcessInstanceQuery[];
  sorting: {
    sortBy:
      | 'instanceId'
      | 'definitionId'
      | 'definitionKey'
      | 'definitionName'
      | 'definitionVersion'
      | 'businessKey'
      | 'startTime'
      | 'endTime'
      | 'duration'
      | 'tenantId';
    sortOrder: 'asc' | 'desc';
  }[];
}
