import { VariableValue } from './variable-value';

export interface TriggerVariableValue<T> extends VariableValue<T> {
  local?: boolean;
}
