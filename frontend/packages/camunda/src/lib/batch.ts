export interface Batch {
  id: string;
  type: string;
  totalJobs: number;
  jobsCreated: number;
  batchJobsPerSeed: number;
  invocationsPerBatchJob: number;
  seedJobDefinitionId: string;
  monitorJobDefinitionId: string;
  batchJobDefinitionId: string;
  suspended: boolean;
  tenantId: string;
  createUserId: string;
}
