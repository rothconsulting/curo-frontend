export interface VariableValue<T> {
  value?: T;
  type?: string;
  valueInfo?: {
    objectTypeName?: string;
    serializationDataFormat?: string;
    filename?: string;
    mimetype?: string;
    encoding?: string;
  };
}
