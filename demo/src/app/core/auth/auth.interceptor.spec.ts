import { HttpRequest } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { AuthInterceptor } from './auth.interceptor';

describe('AuthInterceptor', () => {
  let interceptor: AuthInterceptor;

  beforeEach(() =>
    TestBed.configureTestingModule({
      providers: [AuthInterceptor]
    })
  );

  beforeEach(() => {
    interceptor = TestBed.inject(AuthInterceptor);
  });

  it('should be created', () => {
    expect(interceptor).toBeTruthy();
  });

  it('should add curo basic auth header', () => {
    const request = new HttpRequest('GET', '');
    const next = {
      handle: (req: HttpRequest<any>) => {
        expect(req.headers.get('Authorization')).toEqual(
          'CuroBasic ZGVtbzpkZW1v'
        );
      }
    };

    interceptor.intercept(request, next as any);
  });
});
