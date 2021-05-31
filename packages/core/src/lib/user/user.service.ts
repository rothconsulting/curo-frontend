import { HttpClient, HttpParams } from '@angular/common/http';
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
   * @param filter     Filter to be applied to the result. If not present, all users are returned.
   */
  getUsers(
    attributes: string[] = [],
    filter: {
      emailLike?: string;
      lastnameLike?: string;
      firstnameLike?: string;
      memberOfGroup?: string[];
    } = {}
  ): Observable<User[]> {
    let params = new HttpParams({ fromObject: { attributes, ...filter } });

    return this.httpClient.get<User[]>(`${this.basePath}/users`, {
      params
    });
  }

  /**
   * Get a user.
   *
   * @param id ID of a user
   */
  getUser(id: string) {
    return this.httpClient.get<User>(`${this.basePath}/users/${id}`);
  }

  /**
   * Get current user.
   */
  getCurrentUser() {
    return this.httpClient.get<User>(`${this.basePath}/users/me`);
  }
}
