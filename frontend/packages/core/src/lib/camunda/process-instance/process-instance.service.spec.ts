import {
  HttpClientTestingModule,
  HttpTestingController
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CAMUNDA_BASE_PATH } from '../camunda-base-path';
import { ProcessInstanceService } from './process-instance.service';

describe('ProcessInstanceService', () => {
  let service: ProcessInstanceService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        {
          provide: CAMUNDA_BASE_PATH,
          useValue: '/engine-rest'
        }
      ]
    });
    service = TestBed.inject(ProcessInstanceService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  describe('getProcessInstances', () => {
    it('should get process instances without paging information', () => {
      service.getProcessInstances().subscribe();

      const req = httpTestingController.expectOne(
        '/engine-rest/process-instance'
      );
      expect(req.request.method).toEqual('GET');

      req.flush([]);
    });

    it('should get process instances with maxResult parameter', () => {
      service.getProcessInstances(25).subscribe();

      const req = httpTestingController.expectOne(
        '/engine-rest/process-instance?maxResult=25'
      );
      expect(req.request.method).toEqual('GET');

      req.flush([]);
    });

    it('should get process instances with maxResult and firstResult parameter', () => {
      service.getProcessInstances(50, 50).subscribe();

      const req = httpTestingController.expectOne(
        '/engine-rest/process-instance?maxResult=50&firstResult=50'
      );

      expect(req.request.method).toEqual('GET');

      req.flush([]);
    });
  });

  describe('queryProcessInstances', () => {
    it('should get process instances without query', () => {
      service.queryProcessInstances().subscribe();

      const req = httpTestingController.expectOne(
        '/engine-rest/process-instance'
      );
      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toBeFalsy();

      req.flush([]);
    });

    it('should get process instances with query', () => {
      const query = {
        processDefinitionId: 'aProcessDefinition'
      };

      service.queryProcessInstances(query).subscribe();

      const req = httpTestingController.expectOne(
        '/engine-rest/process-instance'
      );
      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual(query);

      req.flush([]);
    });
  });

  describe('getProcessInstancesCount', () => {
    it('should get the total number of process instances', () => {
      service
        .getProcessInstancesCount()
        .subscribe((data) => expect(data.count).toBe(5));

      const req = httpTestingController.expectOne(
        '/engine-rest/process-instance/count'
      );
      expect(req.request.method).toEqual('GET');

      req.flush({ count: 5 });
    });
  });

  describe('queryProcessInstancesCount', () => {
    it('should query the total number of process instances', () => {
      const query = {
        processDefinitionId: 'aProcessDefinition'
      };

      service
        .queryProcessInstancesCount(query)
        .subscribe((data) => expect(data.count).toBe(6));

      const req = httpTestingController.expectOne(
        '/engine-rest/process-instance/count'
      );
      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual(query);

      req.flush({ count: 6 });
    });
  });

  describe('deleteProcessInstances', () => {
    it('should delete process instances', () => {
      const filter = {
        processInstanceIds: ['1', '2']
      };

      service.deleteProcessInstances(filter).subscribe();

      const req = httpTestingController.expectOne(
        '/engine-rest/process-instance/delete'
      );
      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual(filter);

      req.flush(null);
    });
  });

  describe('deleteProcessInstancesByHistoricQuery', () => {
    it('should delete process instances by historic query', () => {
      const filter = {
        processInstanceIds: ['1', '2']
      };

      service.deleteProcessInstancesByHistoricQuery(filter).subscribe();

      const req = httpTestingController.expectOne(
        '/engine-rest/process-instance/delete-historic-query-based'
      );
      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual(filter);

      req.flush(null);
    });
  });

  describe('setJobRetries', () => {
    it('should set job retries', () => {
      const data = {
        processInstances: ['1', '2']
      };

      service.setJobRetries(data).subscribe();

      const req = httpTestingController.expectOne(
        '/engine-rest/process-instance/job-retries'
      );
      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual(data);

      req.flush(null);
    });
  });

  describe('setJobRetriesByHistoricQuery', () => {
    it('should set job retries by historic query', () => {
      const data = {
        processInstances: ['1', '2']
      };

      service.setJobRetriesByHistoricQuery(data).subscribe();

      const req = httpTestingController.expectOne(
        '/engine-rest/process-instance/job-retries-historic-query-based'
      );
      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual(data);

      req.flush(null);
    });
  });

  describe('suspend', () => {
    it('should update the suspension state', () => {
      const state = {
        suspended: true
      };

      service.suspend(state).subscribe();

      const req = httpTestingController.expectOne(
        '/engine-rest/process-instance/suspended'
      );
      expect(req.request.method).toEqual('PUT');
      expect(req.request.body).toEqual(state);

      req.flush(null);
    });
  });

  describe('suspendAsync', () => {
    it('should update the suspension state async', () => {
      const state = {
        suspended: true
      };

      service.suspendAsync(state).subscribe();

      const req = httpTestingController.expectOne(
        '/engine-rest/process-instance/suspended-async'
      );
      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual(state);

      req.flush(null);
    });
  });

  describe('setVariablesAsync', () => {
    it('should set variables async', () => {
      const variables = {
        processInstanceIds: ['1', '2'],
        variables: {
          varA: {
            value: true
          }
        }
      };

      service.setVariablesAsync(variables).subscribe();

      const req = httpTestingController.expectOne(
        '/engine-rest/process-instance/variables-async'
      );
      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual(variables);

      req.flush(null);
    });
  });

  describe('deleteProcessInstance', () => {
    it('should delete a process instance', () => {
      const instanceId = '2';

      service.deleteProcessInstance(instanceId).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-instance/${instanceId}`
      );
      expect(req.request.method).toEqual('DELETE');

      req.flush(null);
    });
    it('should delete a process instance with options', () => {
      const instanceId = '2';

      service
        .deleteProcessInstance(instanceId, {
          skipCustomListeners: true,
          skipIoMappings: true,
          skipSubprocesses: true,
          failIfNotExists: true
        })
        .subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-instance/${instanceId}?skipCustomListeners=true&skipIoMappings=true&skipSubprocesses=true&failIfNotExists=true`
      );
      expect(req.request.method).toEqual('DELETE');

      req.flush(null);
    });
  });

  describe('getProcessInstance', () => {
    it('should get a process instance', () => {
      const instanceId = '1';

      service.getProcessInstance(instanceId).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-instance/${instanceId}`
      );

      expect(req.request.method).toEqual('GET');

      req.flush(null);
    });
  });

  describe('getActivityInstance', () => {
    it('should get a process instance as activity instance', () => {
      const instanceId = '1';

      service.getActivityInstance(instanceId).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-instance/${instanceId}/activity-instances`
      );

      expect(req.request.method).toEqual('GET');

      req.flush(null);
    });
  });

  describe('modifyProcessInstance', () => {
    it('should modify a process instance', () => {
      const instanceId = '1';
      const data = { skipIoMappings: true };

      service.modifyProcessInstance(instanceId, data).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-instance/${instanceId}/modification`
      );

      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual(data);

      req.flush(null);
    });
  });

  describe('modifyProcessInstanceAsync', () => {
    it('should modify a process instance async', () => {
      const instanceId = '1';
      const data = { skipIoMappings: true };

      service.modifyProcessInstanceAsync(instanceId, data).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-instance/${instanceId}/modification-async`
      );

      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual(data);

      req.flush(null);
    });
  });

  describe('suspendProcessInstance', () => {
    it('should modify a process instance async', () => {
      const instanceId = '1';
      const suspended = true;

      service.suspendProcessInstance(instanceId, suspended).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-instance/${instanceId}/suspended`
      );

      expect(req.request.method).toEqual('PUT');
      expect(req.request.body).toEqual({ suspended });

      req.flush(null);
    });
  });

  describe('getVariables', () => {
    it('should get process instance variables', () => {
      const instanceId = '1';

      service.getVariables(instanceId).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-instance/${instanceId}/variables`
      );

      expect(req.request.method).toEqual('GET');

      req.flush(null);
    });
  });

  describe('patchVariables', () => {
    it('should patch process instance variables', () => {
      const instanceId = '1';
      const modifications = { varA: { value: true } };
      const deletions = ['varB', 'varC'];

      service.patchVariables(instanceId, modifications, deletions).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-instance/${instanceId}/variables`
      );

      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual({ modifications, deletions });

      req.flush(null);
    });
  });

  describe('deleteVariable', () => {
    it('should delete a process instance variable', () => {
      const instanceId = '1';
      const variableName = 'varA';

      service.deleteVariable(instanceId, variableName).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-instance/${instanceId}/variables/${variableName}`
      );

      expect(req.request.method).toEqual('DELETE');

      req.flush(null);
    });
  });

  describe('getVariable', () => {
    it('should get a process instance variable', () => {
      const instanceId = '1';
      const variableName = 'varA';

      service.getVariable(instanceId, variableName).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-instance/${instanceId}/variables/${variableName}`
      );

      expect(req.request.method).toEqual('GET');

      req.flush(null);
    });
  });

  describe('setVariable', () => {
    it('should set a process instance variable', () => {
      const instanceId = '1';
      const variableName = 'varA';
      const value = { value: true };

      service.setVariable(instanceId, variableName, value).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-instance/${instanceId}/variables/${variableName}`
      );

      expect(req.request.method).toEqual('PUT');
      expect(req.request.body).toEqual(value);

      req.flush(null);
    });
  });

  describe('getVariableData', () => {
    it('should get process instance variable data', () => {
      const instanceId = '1';
      const variableName = 'varA';

      service.getVariableData(instanceId, variableName).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-instance/${instanceId}/variables/${variableName}/data`
      );

      expect(req.request.method).toEqual('GET');

      req.flush(null);
    });
  });

  describe('setVariableData', () => {
    it('should set process instance variable data', () => {
      const instanceId = '1';
      const variableName = 'varA';
      const data = 'data';
      const type = 'application/pdf';
      const formData = new FormData();
      formData.append('data', data);
      formData.append('valueType', type);

      service.setVariableData(instanceId, variableName, data, type).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-instance/${instanceId}/variables/${variableName}/data`
      );

      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual(formData);

      req.flush(null);
    });
  });
});
