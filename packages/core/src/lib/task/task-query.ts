import { VariableQueryParameter } from './variable-query-parameter';

export interface TaskQuery {
  sortBy?: string;
  sortOrder?: string;
  processInstanceBusinessKey?: string;
  processInstanceBusinessKeyExpression?: string;
  processInstanceBusinessKeyIn?: string[];
  processInstanceBusinessKeyLike?: string;
  processInstanceBusinessKeyLikeExpression?: string;
  processDefinitionKey?: string;
  processDefinitionKeyIn?: string[];
  processDefinitionId?: string;
  executionId?: string;
  activityInstanceIdIn?: string[];
  processDefinitionName?: string;
  processDefinitionNameLike?: string;
  processInstanceId?: string;
  processInstanceIdIn?: string[];
  assignee?: string;
  assigneeExpression?: string;
  assigneeLike?: string;
  assigneeLikeExpression?: string;
  assigneeIn?: string[];
  assigneeNotIn?: string[];
  candidateGroup?: string;
  candidateGroupExpression?: string;
  candidateUser?: string;
  candidateUserExpression?: string;
  includeAssignedTasks?: boolean;
  taskDefinitionKey?: string;
  taskDefinitionKeyIn?: string[];
  taskDefinitionKeyLike?: string;
  description?: string;
  descriptionLike?: string;
  involvedUser?: string;
  involvedUserExpression?: string;
  maxPriority?: number;
  minPriority?: number;
  name?: string;
  nameNotEqual?: string;
  nameLike?: string;
  nameNotLike?: string;
  owner?: string;
  ownerExpression?: string;
  priority?: number;
  parentTaskId?: string;
  assigned?: boolean;
  unassigned?: boolean;
  active?: boolean;
  suspended?: boolean;
  caseDefinitionKey?: string;
  caseDefinitionId?: string;
  caseDefinitionName?: string;
  caseDefinitionNameLike?: string;
  caseInstanceId?: string;
  caseInstanceBusinessKey?: string;
  caseInstanceBusinessKeyLike?: string;
  caseExecutionId?: string;
  dueAfter?: string;
  dueAfterExpression?: string;
  dueBefore?: string;
  dueBeforeExpression?: string;
  dueDate?: string;
  dueDateExpression?: string;
  followUpAfter?: string;
  followUpAfterExpression?: string;
  followUpBefore?: string;
  followUpBeforeExpression?: string;
  followUpBeforeOrNotExistent?: string;
  followUpBeforeOrNotExistentExpression?: string;
  followUpDate?: string;
  followUpDateExpression?: string;
  createdAfter?: string;
  createdAfterExpression?: string;
  createdBefore?: string;
  createdBeforeExpression?: string;
  createdOn?: string;
  createdOnExpression?: string;
  delegationState?: string;
  tenantIdIn?: string[];
  withoutTenantId?: boolean;
  candidateGroups?: string[];
  candidateGroupsExpression?: string;
  withCandidateGroups?: boolean;
  withoutCandidateGroups?: boolean;
  withCandidateUsers?: boolean;
  withoutCandidateUsers?: boolean;
  variableNamesIgnoreCase?: boolean;
  variableValuesIgnoreCase?: boolean;
  taskVariables?: VariableQueryParameter;
  processVariables?: VariableQueryParameter;
  caseInstanceVariables?: VariableQueryParameter;
  orQueries?: VariableQueryParameter;
  sorting?: {
    sortBy?: string;
    sortOrder?: string;
    parameters?: any;
  }[];
}
