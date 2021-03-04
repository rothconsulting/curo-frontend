import { HttpClient } from '@angular/common/http';
import { Inject, Injectable, Optional } from '@angular/core';
import { Observable } from 'rxjs';
import { CURO_BASE_PATH } from '../curo-base-path';
import { User } from './user';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  basePath: string;

  constructor(
    @Optional() @Inject(CURO_BASE_PATH) curoBasePath: string,
    private httpClient: HttpClient
  ) {
    this.basePath = curoBasePath || '';
  }

  /**
   * Get list of users.
   *
   * @param attributes Define which fields should be returned. If not present, all fields of the user are returned.
   */
  getUsers(attributes?: string[]): Observable<User[]> {
    const params = attributes ? { attributes } : undefined;

    return this.httpClient.get<User[]>(`${this.basePath}/users`, {
      params
    });
  }
}
