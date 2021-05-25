export interface ProcessInstance {
  id: string;
  definitionId: string;
  businessKey: string;
  caseInstanceId: string;
  suspended: boolean;
  tenantId: string;
}
