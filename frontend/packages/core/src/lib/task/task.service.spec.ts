import {
  HttpClientTestingModule,
  HttpTestingController
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CURO_BASE_PATH } from '../curo-base-path';
import { TaskService } from './task.service';

describe('TaskService', () => {
  let service: TaskService;
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
    service = TestBed.inject(TaskService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  describe('getTask', () => {
    it('should get the task for a specific id', () => {
      const id = '1234';

      service.getTask(id).subscribe((data) => expect(data).toEqual({}));

      const req = httpTestingController.expectOne(`/curo-api/tasks/${id}`);
      expect(req.request.method).toEqual('GET');

      req.flush({});
    });

    it('should get the task for a specific id with variables filter', () => {
      const id = '1234';

      service
        .getTask(id, { variables: ['varA', 'varB'] })
        .subscribe((data) => expect(data).toEqual({}));

      const req = httpTestingController.expectOne(
        `/curo-api/tasks/${id}?variables=varA&variables=varB`
      );
      expect(req.request.method).toEqual('GET');

      req.flush({});
    });

    it('should get the task for a specific id with task attributes filter', () => {
      const id = '1234';

      service
        .getTask(id, { attributes: ['attrA', 'attrB'] })
        .subscribe((data) => expect(data).toEqual({}));

      const req = httpTestingController.expectOne(
        `/curo-api/tasks/${id}?attributes=attrA&attributes=attrB`
      );
      expect(req.request.method).toEqual('GET');

      req.flush({});
    });
  });
});
