import { ProcessInstanceModificationInstruction } from './process-instance-modification-instruction';
import { Variables } from './variables';

export interface StartProcessInstance {
  businessKey?: string;
  variables?: Variables;
  caseInstanceId?: string;
  startInstructions?: ProcessInstanceModificationInstruction[];
  skipCustomListeners?: boolean;
  skipIoMappings?: boolean;
  withVariablesInReturn?: boolean;
}
