import { VariableValue } from '../../../../../dist/core/public-api';

export interface TriggerVariableValue<T> extends VariableValue<T> {
  local?: boolean;
}
