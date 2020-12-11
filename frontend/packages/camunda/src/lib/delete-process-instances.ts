import { HistoricProcessInstanceQuery } from './historic-process-instance-query';
import { ProcessInstanceQuery } from './process-instance-query';

export interface DeleteProcessInstances {
  processInstanceIds?: string[];
  deleteReason?: string;
  skipCustomListeners?: boolean;
  skipSubprocesses?: boolean;
  processInstanceQuery?: ProcessInstanceQuery;
  historicProcessInstanceQuery?: HistoricProcessInstanceQuery;
}
