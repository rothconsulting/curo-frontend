import { VariableQueryParameter } from './variable-query-parameter';

export interface ProcessInstanceQuery {
  deploymentId?: string;
  processDefinitionId?: string;
  processDefinitionKey?: string;
  processDefinitionKeyIn?: string[];
  processDefinitionKeyNotIn?: string[];
  businessKey?: string;
  businessKeyLike?: string;
  caseInstanceId?: string;
  superProcessInstance?: string;
  subProcessInstance?: string;
  superCaseInstance?: string;
  subCaseInstance?: string;
  active?: boolean;
  suspended?: boolean;
  processInstanceIds?: string[];
  withIncident?: boolean;
  incidentId?: string;
  incidentType?: string;
  incidentMessage?: string;
  incidentMessageLike?: string;
  tenantIdIn?: string[];
  withoutTenantId?: boolean;
  processDefinitionWithoutTenantId?: boolean;
  activityIdIn?: string[];
  rootProcessInstances?: boolean;
  leafProcessInstances?: boolean;
  variables?: VariableQueryParameter[];
  variableNamesIgnoreCase?: boolean;
  variableValuesIgnoreCase?: boolean;
  orQueries?: ProcessInstanceQuery[];
  sorting?: {
    sortBy?:
      | 'instanceId'
      | 'definitionId'
      | 'definitionKey'
      | 'businessKey'
      | 'tenantId';
    sortOrder?: 'asc' | 'desc';
  }[];
}
