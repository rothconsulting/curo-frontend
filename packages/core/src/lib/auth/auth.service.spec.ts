import {
  HttpClientTestingModule,
  HttpTestingController
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CURO_BASE_PATH } from '../curo-base-path';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
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
    service = TestBed.inject(AuthService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should trigger login success logic', () => {
    service.confirmAuthSuccess().subscribe((auth) => expect(auth).toEqual({}));

    const req = httpTestingController.expectOne(`/curo-api/auth/success`);
    expect(req.request.method).toEqual('POST');
    expect(req.request.body).toEqual(null);

    req.flush({});
  });

  it('should load permissions', () => {
    const requestedPermissions = {
      '*': {
        PROCESS_DEFINITION: ['READ'],
        PROCESS_INSTANCE: ['*']
      }
    };
    const mock = {
      userId: 'test',
      groups: ['admin'],
      permissions: {},
      curoPermissions: {}
    };

    service
      .loadPermissions(requestedPermissions)
      .subscribe((auth) => expect(auth).toEqual(mock));

    const req = httpTestingController.expectOne(`/curo-api/auth/permissions`);
    expect(req.request.method).toEqual('POST');
    expect(req.request.body).toEqual(requestedPermissions);

    req.flush(mock);
  });
});
