import {
  HttpClientTestingModule,
  HttpTestingController
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CURO_BASE_PATH } from '../curo-base-path';
import { MenuService } from './menu.service';

describe('MenuService', () => {
  let service: MenuService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        {
          provide: CURO_BASE_PATH,
          useValue: '/curo-api'
        }
      ]
    });
    service = TestBed.inject(MenuService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should get the menu', () => {
    service.getMenu().subscribe((menu) => expect(menu).toEqual([]));

    const req = httpTestingController.expectOne(`/curo-api/menus`);
    expect(req.request.method).toEqual('GET');

    req.flush([]);
  });
});
