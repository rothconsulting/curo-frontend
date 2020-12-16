import {
  HttpClientTestingModule,
  HttpTestingController
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CURO_BASE_PATH } from '../curo-base-path';
import { ProcessService } from './process.service';

describe('ProcessService', () => {
  let service: ProcessService;
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
    service = TestBed.inject(ProcessService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  describe('startProcess', () => {
    it('should start', () => {
      const processDefinitionKey = 'TestProcess';
      const businessKey = '1234';
      const variables = {
        varA: 'Test'
      };

      service
        .startProcess(processDefinitionKey, variables, businessKey, {
          returnVariables: true
        })
        .subscribe();

      const req = httpTestingController.expectOne(
        `/curo-api/process-instances?returnVariables=true`
      );
      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual({
        processDefinitionKey,
        businessKey,
        variables
      });

      req.flush({});
    });
  });
});
