import { ActivityInstanceIncident } from './activity-instance-incident';

export interface TransitionInstance {
  id?: string;
  parentActivityInstanceId?: string;
  activityId?: string;
  activityName?: string;
  activityType?: string;
  processInstanceId?: string;
  processDefinitionId?: string;
  executionId?: string;
  incidentIds?: string[];
  incidents?: ActivityInstanceIncident[];
}
