import { HttpClient } from '@angular/common/http';
import { Inject, Injectable, Optional } from '@angular/core';
import { Observable } from 'rxjs';
import { CURO_BASE_PATH } from '../curo-base-path';
import { AuthSuccess } from './auth-success';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  basePath: string;

  constructor(
    @Optional() @Inject(CURO_BASE_PATH) curoBasePath: string,
    private httpClient: HttpClient
  ) {
    this.basePath = curoBasePath || '';
  }

  /**
   * Trigger login success logic.
   */
  confirmAuthSuccess(): Observable<AuthSuccess> {
    return this.httpClient.post<AuthSuccess>(
      `${this.basePath}/auth/success`,
      null
    );
  }
}
