import { HistoricProcessInstanceQuery } from './historic-process-instance-query';
import { ProcessInstanceQuery } from './process-instance-query';

export interface SetJobRetriesByProcess {
  processInstances?: string[];
  retries?: number;
  processInstanceQuery?: ProcessInstanceQuery;
  historicProcessInstanceQuery?: HistoricProcessInstanceQuery;
}
