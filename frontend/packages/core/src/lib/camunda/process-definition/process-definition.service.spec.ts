import {
  HttpClientTestingModule,
  HttpTestingController
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CAMUNDA_BASE_PATH } from '../camunda-base-path';
import { ProcessDefinitionService } from './process-definition.service';

describe('ProcessDefinitionService', () => {
  let service: ProcessDefinitionService;
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
    service = TestBed.inject(ProcessDefinitionService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  describe('getProcessDefinitions', () => {
    it('should get all process definitions without params', () => {
      service.getProcessDefinitions().subscribe();

      const req = httpTestingController.expectOne(
        '/engine-rest/process-definition'
      );
      expect(req.request.method).toEqual('GET');

      req.flush([]);
    });

    it('should get all process definitions with params', () => {
      service
        .getProcessDefinitions({ sortBy: 'name', sortOrder: 'asc' }, 50, 100)
        .subscribe();

      const req = httpTestingController.expectOne(
        '/engine-rest/process-definition?maxResult=50&firstResult=100&sortBy=name&sortOrder=asc'
      );
      expect(req.request.method).toEqual('GET');

      req.flush([]);
    });
  });

  describe('getProcessDefinitionsCount', () => {
    it('should count process definitions without params', () => {
      service.getProcessDefinitionsCount().subscribe();

      const req = httpTestingController.expectOne(
        '/engine-rest/process-definition/count'
      );
      expect(req.request.method).toEqual('GET');

      req.flush(null);
    });

    it('should count process definitions with params', () => {
      service
        .getProcessDefinitionsCount({ sortBy: 'name', sortOrder: 'asc' })
        .subscribe();

      const req = httpTestingController.expectOne(
        '/engine-rest/process-definition/count?sortBy=name&sortOrder=asc'
      );
      expect(req.request.method).toEqual('GET');

      req.flush(null);
    });
  });

  describe('deleteProcessDefinitionByKey', () => {
    it('should delete process definition without params', () => {
      const key = 'aProcessDefinition';
      service.deleteProcessDefinitionByKey(key).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-definition/key/${key}`
      );
      expect(req.request.method).toEqual('DELETE');

      req.flush(null);
    });

    it('should delete process definition with params', () => {
      const key = 'aProcessDefinition';
      service.deleteProcessDefinitionByKey(key, true, true, true).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-definition/key/${key}?cascade=true&skipCustomListeners=true&skipIoMappings=true`
      );
      expect(req.request.method).toEqual('DELETE');

      req.flush(null);
    });
  });

  describe('getProcessDefinitionByKey', () => {
    it('should get a process definition by key', () => {
      const key = 'aProcessDefinition';
      service.getProcessDefinitionByKey(key).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-definition/key/${key}`
      );
      expect(req.request.method).toEqual('GET');

      req.flush(null);
    });
  });

  describe('getDiagramByKey', () => {
    it('should get a process definition diagram by key', () => {
      const key = 'aProcessDefinition';
      service.getDiagramByKey(key).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-definition/key/${key}/diagram`
      );
      expect(req.request.method).toEqual('GET');

      req.flush(null);
    });
  });

  describe('getStartFormVariablesByKey', () => {
    it('should get start form variables by key', () => {
      const key = 'aProcessDefinition';
      service.getStartFormVariablesByKey(key).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-definition/key/${key}/form-variables`
      );
      expect(req.request.method).toEqual('GET');

      req.flush(null);
    });
  });

  describe('updateHistoryTimeToLiveByKey', () => {
    it('should update history time to live by key', () => {
      const key = 'aProcessDefinition';
      const historyTimeToLive = 100;
      service.updateHistoryTimeToLiveByKey(key, historyTimeToLive).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-definition/key/${key}/history-time-to-live`
      );
      expect(req.request.method).toEqual('PUT');
      expect(req.request.body).toEqual({ historyTimeToLive });

      req.flush(null);
    });
  });

  describe('startByKey', () => {
    it('should start a process definition by key without data', () => {
      const key = 'aProcessDefinition';
      service.startByKey(key).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-definition/key/${key}/start`
      );
      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual(null);

      req.flush(null);
    });

    it('should start a process definition by key with data', () => {
      const key = 'aProcessDefinition';
      const data = {
        variables: {
          varA: {
            value: 'A'
          }
        }
      };
      service.startByKey(key, data).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-definition/key/${key}/start`
      );
      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual(data);

      req.flush(null);
    });
  });

  describe('getStartFormByKey', () => {
    it('should get a process definition start form by key', () => {
      const key = 'aProcessDefinition';
      service.getStartFormByKey(key).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-definition/key/${key}/startForm`
      );
      expect(req.request.method).toEqual('GET');

      req.flush(null);
    });
  });

  describe('getStatisticsByKey', () => {
    it('should get statistics by key without filter', () => {
      const key = 'aProcessDefinition';
      service.getStatisticsByKey(key).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-definition/key/${key}/statistics`
      );
      expect(req.request.method).toEqual('GET');

      req.flush(null);
    });

    it('should get statistics by key with filter', () => {
      const key = 'aProcessDefinition';
      service
        .getStatisticsByKey(key, {
          failedJobs: true,
          incidents: true,
          incidentsForType: 'customType'
        })
        .subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-definition/key/${key}/statistics?failedJobs=true&incidents=true&incidentsForType=customType`
      );
      expect(req.request.method).toEqual('GET');

      req.flush(null);
    });
  });

  describe('suspendByKey', () => {
    it('should suspend a process definition by key', () => {
      const key = 'aProcessDefinition';
      const data = {
        suspended: true
      };
      service.suspendByKey(key, data).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-definition/key/${key}/suspended`
      );
      expect(req.request.method).toEqual('PUT');
      expect(req.request.body).toEqual(data);

      req.flush(null);
    });
  });

  describe('getXmlByKey', () => {
    it('should get a process definition xml by key', () => {
      const key = 'aProcessDefinition';
      service.getXmlByKey(key).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-definition/key/${key}/xml`
      );
      expect(req.request.method).toEqual('GET');

      req.flush(null);
    });
  });

  describe('getStatistics', () => {
    it('should get statistics without filter', () => {
      service.getStatistics().subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-definition/statistics`
      );
      expect(req.request.method).toEqual('GET');

      req.flush(null);
    });

    it('should get statistics with filter', () => {
      service
        .getStatistics({
          failedJobs: true,
          incidents: true,
          incidentsForType: 'customType',
          rootIncidents: true
        })
        .subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-definition/statistics?failedJobs=true&incidents=true&incidentsForType=customType&rootIncidents=true`
      );
      expect(req.request.method).toEqual('GET');

      req.flush(null);
    });
  });

  describe('suspend', () => {
    it('should suspend a process definition', () => {
      const data = {
        suspended: true,
        processDefinitionKey: 'aProcessDefinition'
      };
      service.suspend(data).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-definition/suspended`
      );
      expect(req.request.method).toEqual('PUT');
      expect(req.request.body).toEqual(data);

      req.flush(null);
    });
  });

  describe('deleteProcessDefinitionById', () => {
    it('should delete process definition without params', () => {
      const id = 'aProcessId';
      service.deleteProcessDefinitionById(id).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-definition/${id}`
      );
      expect(req.request.method).toEqual('DELETE');

      req.flush(null);
    });

    it('should delete process definition with params', () => {
      const id = 'aProcessId';
      service.deleteProcessDefinitionById(id, true, true, true).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-definition/${id}?cascade=true&skipCustomListeners=true&skipIoMappings=true`
      );
      expect(req.request.method).toEqual('DELETE');

      req.flush(null);
    });
  });

  describe('getProcessDefinitionById', () => {
    it('should get a process definition by id', () => {
      const id = 'aProcessId';
      service.getProcessDefinitionById(id).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-definition/${id}`
      );
      expect(req.request.method).toEqual('GET');

      req.flush(null);
    });
  });

  describe('getDiagramById', () => {
    it('should get a process definition diagram by id', () => {
      const id = 'aProcessId';
      service.getDiagramById(id).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-definition/${id}/diagram`
      );
      expect(req.request.method).toEqual('GET');

      req.flush(null);
    });
  });

  describe('getStartFormVariablesById', () => {
    it('should get start form variables by id', () => {
      const id = 'aProcessId';
      service.getStartFormVariablesById(id).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-definition/${id}/form-variables`
      );
      expect(req.request.method).toEqual('GET');

      req.flush(null);
    });
  });

  describe('updateHistoryTimeToLiveById', () => {
    it('should update history time to live by id', () => {
      const id = 'aProcessId';
      const historyTimeToLive = 100;
      service.updateHistoryTimeToLiveById(id, historyTimeToLive).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-definition/${id}/history-time-to-live`
      );
      expect(req.request.method).toEqual('PUT');
      expect(req.request.body).toEqual({ historyTimeToLive });

      req.flush(null);
    });
  });

  describe('restartProcessInstanceById', () => {
    it('should restart process instances by id', () => {
      const id = 'aProcessId';
      const data = {
        processInstanceIds: ['1', '2']
      };
      service.restartProcessInstanceById(id, data).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-definition/${id}/restart`
      );
      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual(data);

      req.flush(null);
    });
  });

  describe('restartProcessInstanceAsyncById', () => {
    it('should restart process instances async by id', () => {
      const id = 'aProcessId';
      const data = {
        processInstanceIds: ['1', '2']
      };
      service.restartProcessInstanceAsyncById(id, data).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-definition/${id}/restart-async`
      );
      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual(data);

      req.flush(null);
    });
  });

  describe('startById', () => {
    it('should start a process definition by id without data', () => {
      const id = 'aProcessId';
      service.startById(id).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-definition/${id}/start`
      );
      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual(null);

      req.flush(null);
    });

    it('should start a process definition by id with data', () => {
      const id = 'aProcessId';
      const data = {
        variables: {
          varA: {
            value: 'A'
          }
        }
      };
      service.startById(id, data).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-definition/${id}/start`
      );
      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual(data);

      req.flush(null);
    });
  });

  describe('getStartFormById', () => {
    it('should get a process definition start form by id', () => {
      const id = 'aProcessId';
      service.getStartFormById(id).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-definition/${id}/startForm`
      );
      expect(req.request.method).toEqual('GET');

      req.flush(null);
    });
  });

  describe('getStatisticsById', () => {
    it('should get statistics by id without filter', () => {
      const id = 'aProcessId';
      service.getStatisticsById(id).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-definition/${id}/statistics`
      );
      expect(req.request.method).toEqual('GET');

      req.flush(null);
    });

    it('should get statistics by id with filter', () => {
      const id = 'aProcessId';
      service
        .getStatisticsById(id, {
          failedJobs: true,
          incidents: true,
          incidentsForType: 'customType'
        })
        .subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-definition/${id}/statistics?failedJobs=true&incidents=true&incidentsForType=customType`
      );
      expect(req.request.method).toEqual('GET');

      req.flush(null);
    });
  });

  describe('suspendById', () => {
    it('should suspend a process definition by id', () => {
      const id = 'aProcessId';
      const data = {
        suspended: true
      };
      service.suspendById(id, data).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-definition/${id}/suspended`
      );
      expect(req.request.method).toEqual('PUT');
      expect(req.request.body).toEqual(data);

      req.flush(null);
    });
  });

  describe('getXmlById', () => {
    it('should get a process definition xml by id', () => {
      const id = 'aProcessId';
      service.getXmlById(id).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/process-definition/${id}/xml`
      );
      expect(req.request.method).toEqual('GET');

      req.flush(null);
    });
  });
});
