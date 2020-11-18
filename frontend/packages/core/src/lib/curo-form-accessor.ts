import { InjectionToken, Type } from '@angular/core';

// tslint:disable-next-line: no-empty-interface
export interface CuroFormAccessor {}

export const CURO_FORM_ACCESSOR = new InjectionToken<Type<CuroFormAccessor>[]>(
  'CURO_FORM_ACCESSOR'
);
