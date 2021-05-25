import { HttpClient } from '@angular/common/http';
import { Inject, Injectable, Optional } from '@angular/core';
import { Observable } from 'rxjs';
import { CURO_BASE_PATH } from '../curo-base-path';
import { MenuElement } from './menu-element';

@Injectable({
  providedIn: 'root'
})
export class MenuService {
  basePath: string;

  constructor(
    @Optional() @Inject(CURO_BASE_PATH) curoBasePath: string,
    private httpClient: HttpClient
  ) {
    this.basePath = curoBasePath || '';
  }

  /**
   * Get menu for the current user.
   */
  getMenu(): Observable<MenuElement[]> {
    return this.httpClient.get<MenuElement[]>(`${this.basePath}/menus`);
  }
}
