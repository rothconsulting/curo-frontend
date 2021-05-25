import { HistoricProcessInstanceQuery } from './historic-process-instance-query';
import { RestartProcessInstanceModificationInstruction } from './restart-process-instance-modification-instruction';

export interface RestartProcessInstance {
  processInstanceIds?: string[];
  historicProcessInstanceQuery?: HistoricProcessInstanceQuery;
  skipCustomListeners?: boolean;
  skipIoMappings?: boolean;
  initialVariables?: boolean;
  withoutBusinessKey?: boolean;
  instructions?: RestartProcessInstanceModificationInstruction[];
}
