import { HistoricProcessInstanceQuery } from './historic-process-instance-query';
import { ProcessInstanceQuery } from './process-instance-query';
import { Variables } from './variables';

export interface SetVariablesAsync {
  processInstanceIds?: string[];
  processInstanceQuery?: ProcessInstanceQuery;
  historicProcessInstanceQuery?: HistoricProcessInstanceQuery;
  variables?: Variables;
}
