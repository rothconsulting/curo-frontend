import { HistoricProcessInstanceQuery } from './historic-process-instance-query';
import { ProcessInstanceQuery } from './process-instance-query';

export interface ProcessInstanceSuspensionState {
  suspended?: boolean;
  processDefinitionId?: string;
  processDefinitionKey?: string;
  processDefinitionTenantId?: string;
  processDefinitionWithoutTenantId?: boolean;
  processInstanceIds?: string[];
  processInstanceQuery?: ProcessInstanceQuery;
  historicProcessInstanceQuery?: HistoricProcessInstanceQuery;
}
