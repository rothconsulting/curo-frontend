import { ActivityInstanceIncident } from './activity-instance-incident';
import { TransitionInstance } from './transition-instance';

export interface ActivityInstance {
  id?: string;
  parentActivityInstanceId?: string;
  activityId?: string;
  activityName?: string;
  activityType?: string;
  processInstanceId?: string;
  processDefinitionId?: string;
  childActivityInstances?: ActivityInstance[];
  childTransitionInstances?: TransitionInstance[];
  executionIds?: string[];
  incidentIds?: string[];
  incidents?: ActivityInstanceIncident[];
}
