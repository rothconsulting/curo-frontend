export interface VariableQueryParameter {
  name?: string;
  operator?: 'eq' | 'neq' | 'gt' | 'gteq' | 'lt' | 'lteq' | 'like';
  value?: boolean | string | number;
}
