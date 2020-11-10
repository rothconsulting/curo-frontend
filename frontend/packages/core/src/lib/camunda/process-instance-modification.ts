import { ProcessInstanceModificationInstruction } from './process-instance-modification-instruction';

export interface ProcessInstanceModification {
  skipCustomListeners?: boolean;
  skipIoMappings?: boolean;
  instructions?: ProcessInstanceModificationInstruction[];
  annotation?: string;
}
