import {
  HttpClientTestingModule,
  HttpTestingController
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CURO_BASE_PATH } from '../curo-base-path';
import { UserService } from './user.service';

describe('UserService', () => {
  let service: UserService;
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
    service = TestBed.inject(UserService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should get the users', () => {
    service.getUsers().subscribe((users) => expect(users).toEqual([]));

    const req = httpTestingController.expectOne(`/curo-api/users`);
    expect(req.request.method).toEqual('GET');

    req.flush([]);
  });

  it('should get a user', () => {
    const userA = {
      id: 'userA',
      firstname: 'User',
      lastname: 'A',
      email: 'user.a@me.com'
    };
    service.getUser('userA').subscribe((user) => expect(user).toEqual(userA));

    const req = httpTestingController.expectOne(`/curo-api/users/userA`);
    expect(req.request.method).toEqual('GET');

    req.flush(userA);
  });

  it('should get current user', () => {
    const myUser = {
      id: 'myUser',
      firstname: 'My',
      lastname: 'User',
      email: 'my.user@me.com'
    };
    service.getCurrentUser().subscribe((user) => expect(user).toEqual(myUser));

    const req = httpTestingController.expectOne(`/curo-api/users/me`);
    expect(req.request.method).toEqual('GET');

    req.flush(myUser);
  });
});
