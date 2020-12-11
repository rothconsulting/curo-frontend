import { IncidentStatisticsResult } from './incident-statistics-result';

export interface ActivityStatisticsResult {
  id: string;
  instances: number;
  failedJobs: number;
  incidents: IncidentStatisticsResult[];
}
