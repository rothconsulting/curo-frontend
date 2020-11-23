import { IncidentStatisticsResult } from './incident-statistics-result';
import { ProcessDefinition } from './process-definition';

export interface ProcessDefinitionStatisticsResult {
  id: string;
  instances: number;
  failedJobs: number;
  incidents: IncidentStatisticsResult[];
  definition: ProcessDefinition;
}
