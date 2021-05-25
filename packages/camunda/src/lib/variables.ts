import { VariableValue } from './variable-value';

export interface Variables {
  [key: string]: VariableValue<unknown>;
}
