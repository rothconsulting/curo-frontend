import { InjectionToken, Type } from '@angular/core';

// tslint:disable-next-line: no-empty-interface
export interface CuroFormAccessor {
  key: string;
  component: Type<any>;
}

export const CURO_FORM_ACCESSOR = new InjectionToken<Type<CuroFormAccessor>[]>(
  'CURO_FORM_ACCESSOR'
);
