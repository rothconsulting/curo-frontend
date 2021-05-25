import { ProcessInstance } from './process-instance';
import { Variables } from './variables';

export interface ProcessInstanceWithVariables extends ProcessInstance {
  variables: Variables;
}
