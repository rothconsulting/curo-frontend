import { TriggerVariableValue } from './trigger-variable-value';

export interface ProcessInstanceModificationInstruction {
  type:
    | 'cancel'
    | 'startBeforeActivity'
    | 'startAfterActivity'
    | 'startTransition';
  variables: TriggerVariableValue<unknown>;
  activityId: string;
  transitionId: string;
  activityInstanceId: string;
  transitionInstanceId: string;
  ancestorActivityInstanceId: string;
  cancelCurrentActiveActivityInstances?: boolean;
}
