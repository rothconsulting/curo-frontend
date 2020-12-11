import { HistoricProcessInstanceQuery } from './historic-process-instance-query';
import { ProcessInstanceQuery } from './process-instance-query';

export interface ProcessInstanceSuspensionStateAsync {
  suspended?: boolean;
  processInstanceIds?: string[];
  processInstanceQuery?: ProcessInstanceQuery;
  historicProcessInstanceQuery?: HistoricProcessInstanceQuery;
}
