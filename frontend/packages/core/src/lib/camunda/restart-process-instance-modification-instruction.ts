export interface RestartProcessInstanceModificationInstruction {
  type: 'startBeforeActivity' | 'startAfterActivity' | 'startTransition';
  activityId?: string;
  transitionId?: string;
}
