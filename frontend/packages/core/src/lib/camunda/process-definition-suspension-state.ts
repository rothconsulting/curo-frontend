export interface ProcessDefinitionSuspensionState {
  suspended?: boolean;
  processDefinitionId?: string;
  processDefinitionKey?: string;
  includeProcessInstances?: boolean;
  executionDate?: string;
}
