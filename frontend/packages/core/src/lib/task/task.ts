export interface Task {
  id?: string;
  name?: string;
  assignee?: string;
  created?: string;
  due?: string;
  followUp?: string;
  delegationState?: string;
  description?: string;
  executionId?: string;
  owner?: string;
  parentTaskId?: string;
  priority?: number;
  processDefinitionId?: string;
  processInstanceId?: string;
  taskDefinitionKey?: string;
  suspended?: boolean;
  formKey?: string;
  variables?: {
    [key: string]: any;
  };
}
