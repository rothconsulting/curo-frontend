import { HttpClient } from '@angular/common/http';
import { Inject, Injectable, Optional } from '@angular/core';
import { Observable } from 'rxjs';
import { CURO_BASE_PATH } from '../curo-base-path';
import { AuthSuccess } from './auth-success';
import { Permissions } from './permissions';

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

  /**
   * Load permissions
   */
  loadPermissions(
    requestedPermissions?: any,
    options?: { returnPermissions: boolean }
  ): Observable<Permissions> {
    return this.httpClient.post<Permissions>(
      `${this.basePath}/auth/permissions`,
      requestedPermissions,
      { params: options as any }
    );
  }
}
