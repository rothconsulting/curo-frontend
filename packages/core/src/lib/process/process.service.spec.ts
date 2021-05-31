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

  describe('startProcessWithFiles', () => {
    it('should start with files', () => {
      const processDefinitionKey = 'TestProcess';
      const businessKey = '1234';
      const variables = {
        varA: 'Test'
      };
      const files = {
        myFileA: new File([], 'file-a.pdf'),
        myFileB: new File([], 'file-b.pdf')
      };

      const expectedFormData = new FormData();
      expectedFormData.append(
        'processStartData',
        JSON.stringify({
          processDefinitionKey,
          businessKey,
          variables
        })
      );
      expectedFormData.append('myFileA', files.myFileA);
      expectedFormData.append('myFileB', files.myFileB);

      service
        .startProcessWithFiles(
          processDefinitionKey,
          variables,
          files,
          businessKey,
          {
            returnVariables: true
          }
        )
        .subscribe();

      const req = httpTestingController.expectOne(
        `/curo-api/process-instances?returnVariables=true`
      );
      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual(expectedFormData);

      req.flush({});
    });
  });

  describe('nextTask', () => {
    it('should query the next user task', () => {
      const id = '1234';

      service.nextTask(id, true).subscribe((data) => expect(data).toEqual({}));

      const req = httpTestingController.expectOne(
        `/curo-api/process-instances/${id}/next?flowToNextIgnoreAssignee=true`
      );
      expect(req.request.method).toEqual('GET');

      req.flush({});
    });
  });
});
