import {
  HttpClientTestingModule,
  HttpTestingController
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CAMUNDA_BASE_PATH } from '../camunda-base-path';
import { Task } from '../task';
import { TaskService } from './task.service';

describe('TaskService', () => {
  let service: TaskService;
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
    service = TestBed.inject(TaskService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  describe('getTasks', () => {
    it('should get tasks without paging information', () => {
      service.getTasks().subscribe();

      const req = httpTestingController.expectOne('/engine-rest/task');
      expect(req.request.method).toEqual('GET');

      req.flush([]);
    });

    it('should get tasks with maxResult parameter', () => {
      service.getTasks(25).subscribe();

      const req = httpTestingController.expectOne(
        '/engine-rest/task?maxResult=25'
      );
      expect(req.request.method).toEqual('GET');

      req.flush([]);
    });

    it('should get tasks with maxResult and firstResult parameter', () => {
      service.getTasks(50, 50).subscribe();

      const req = httpTestingController.expectOne(
        '/engine-rest/task?maxResult=50&firstResult=50'
      );

      expect(req.request.method).toEqual('GET');

      req.flush([]);
    });
  });

  describe('queryTasks', () => {
    it('should get tasks without query', () => {
      service.queryTasks().subscribe();

      const req = httpTestingController.expectOne('/engine-rest/task');
      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toBeFalsy();

      req.flush([]);
    });

    it('should get tasks with query', () => {
      const query = {
        assignee: 'me'
      };

      service.queryTasks(query).subscribe();

      const req = httpTestingController.expectOne('/engine-rest/task');
      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual(query);

      req.flush([]);
    });
  });

  describe('getTasksCount', () => {
    it('should get the total number of tasks', () => {
      service.getTasksCount().subscribe((data) => expect(data.count).toBe(5));

      const req = httpTestingController.expectOne('/engine-rest/task/count');
      expect(req.request.method).toEqual('GET');

      req.flush({ count: 5 });
    });
  });

  describe('queryTasksCount', () => {
    it('should query the total number of tasks', () => {
      const query = {
        assignee: 'me'
      };

      service
        .queryTasksCount(query)
        .subscribe((data) => expect(data.count).toBe(6));

      const req = httpTestingController.expectOne('/engine-rest/task/count');
      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual(query);

      req.flush({ count: 6 });
    });
  });

  describe('createTask', () => {
    it('should create a task', () => {
      const task = {
        assignee: 'me'
      } as Task;

      service.createTask(task).subscribe();

      const req = httpTestingController.expectOne('/engine-rest/task/create');
      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual(task);

      req.flush(null);
    });
  });

  describe('deleteTask', () => {
    it('should delete a task', () => {
      const taskId = '2';

      service.deleteTask(taskId).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/task/${taskId}`
      );
      expect(req.request.method).toEqual('DELETE');

      req.flush(null);
    });
  });

  describe('updateTask', () => {
    it('should update a task', () => {
      const taskId = '2';
      const task = {
        assignee: 'me'
      } as Task;

      service.updateTask(taskId, task).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/task/${taskId}`
      );
      expect(req.request.method).toEqual('PUT');

      req.flush(null);
    });
  });

  describe('getTask', () => {
    it('should get a task', () => {
      const testData = {} as Task;
      service
        .getTask('123')
        .subscribe((data) => expect(data).toEqual(testData));

      const req = httpTestingController.expectOne('/engine-rest/task/123');

      expect(req.request.method).toEqual('GET');

      req.flush(testData);
    });
  });

  describe('assignTask', () => {
    it('should assign a task', () => {
      const taskId = '2';
      const userId = 'user-1';

      service.assignTask(taskId, userId).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/task/${taskId}/assignee`
      );
      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual({ userId });

      req.flush(null);
    });
  });

  describe('claimTask', () => {
    it('should claim a task', () => {
      const taskId = '2';
      const userId = 'user-1';

      service.claimTask(taskId, userId).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/task/${taskId}/claim`
      );
      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual({ userId });

      req.flush(null);
    });
  });

  describe('completeTask', () => {
    it('should complete a task', () => {
      const taskId = '2';
      const variables = {
        myVariable: {
          value: true,
          type: 'Boolean'
        }
      };

      service.completeTask(taskId, variables).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/task/${taskId}/complete`
      );
      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual({ variables });

      req.flush(null);
    });
  });

  describe('delegateTask', () => {
    it('should delegate a task', () => {
      const taskId = '3';
      const userId = 'user-2';

      service.delegateTask(taskId, userId).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/task/${taskId}/delegate`
      );
      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual({ userId });

      req.flush(null);
    });
  });

  describe('getForm', () => {
    it('should get a tasks form', () => {
      const taskId = '3';
      const mock = {
        key: 'FormComponent',
        contextPath: 'context/path'
      };

      service.getForm(taskId).subscribe((data) => expect(data).toEqual(mock));

      const req = httpTestingController.expectOne(
        `/engine-rest/task/${taskId}/form`
      );
      expect(req.request.method).toEqual('GET');

      req.flush(mock);
    });
  });

  describe('getFormVariables', () => {
    it('should get all form variables', () => {
      const taskId = '3';
      const mock = {
        myVar: {
          value: true
        }
      };

      service
        .getFormVariables(taskId)
        .subscribe((data) => expect(data).toEqual(mock));

      const req = httpTestingController.expectOne(
        `/engine-rest/task/${taskId}/form-variables`
      );
      expect(req.request.method).toEqual('GET');

      req.flush(mock);
    });

    it('should get requested form variables', () => {
      const taskId = '3';
      const mock = {
        myVar: {
          value: true
        }
      };
      const varialbeNames = ['var1', 'var2', 'var3'];

      service
        .getFormVariables(taskId, varialbeNames)
        .subscribe((data) => expect(data).toEqual(mock));

      const req = httpTestingController.expectOne(
        `/engine-rest/task/${taskId}/form-variables?variableNames=${varialbeNames.join(
          ','
        )}`
      );
      expect(req.request.method).toEqual('GET');

      req.flush(mock);
    });
  });

  describe('resolveTask', () => {
    it('should resolve a task', () => {
      const taskId = '3';
      const variables = {
        myVar: {
          value: true
        }
      };

      service.resolveTask(taskId, variables).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/task/${taskId}/resolve`
      );
      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual({ variables });

      req.flush(null);
    });
  });

  describe('unclaimTask', () => {
    it('should unclaim a task', () => {
      const taskId = '3';

      service.unclaimTask(taskId).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/task/${taskId}/unclaim`
      );
      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toBeNull();

      req.flush(null);
    });
  });

  describe('getAttachments', () => {
    it('should get all attachments', () => {
      const taskId = '3';

      service
        .getAttachments(taskId)
        .subscribe((data) => expect(data).toEqual([]));

      const req = httpTestingController.expectOne(
        `/engine-rest/task/${taskId}/attachment`
      );
      expect(req.request.method).toEqual('GET');

      req.flush([]);
    });
  });

  describe('createAttachment', () => {
    it('should create an attachment', () => {
      const taskId = '3';

      const formData = new FormData();
      formData.append('attachment-name', 'name');
      formData.append('attachment-description', 'description');
      formData.append('attachment-type', 'mimetype');
      formData.append('url', 'url');
      formData.append('content', 'content');

      service
        .createAttachment(
          taskId,
          'name',
          'description',
          'mimetype',
          'url',
          'content'
        )
        .subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/task/${taskId}/attachment/create`
      );
      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual(formData);

      req.flush(null);
    });
  });

  describe('deleteAttachment', () => {
    it('should delete an attachment', () => {
      const taskId = '3';
      const attachmentId = '5';

      service.deleteAttachment(taskId, attachmentId).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/task/${taskId}/attachment/${attachmentId}`
      );
      expect(req.request.method).toEqual('DELETE');

      req.flush(null);
    });
  });

  describe('getAttachment', () => {
    it('should get an attachment', () => {
      const taskId = '3';
      const attachmentId = '5';

      service.getAttachment(taskId, attachmentId).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/task/${taskId}/attachment/${attachmentId}`
      );
      expect(req.request.method).toEqual('GET');

      req.flush(null);
    });
  });

  describe('getAttachmentData', () => {
    it('should get data of an attachment', () => {
      const taskId = '3';
      const attachmentId = '5';

      service.getAttachmentData(taskId, attachmentId).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/task/${taskId}/attachment/${attachmentId}/data`
      );
      expect(req.request.method).toEqual('GET');

      req.flush(null);
    });
  });

  describe('getComments', () => {
    it('should get all comments', () => {
      const taskId = '3';

      service.getComments(taskId).subscribe((data) => expect(data).toEqual([]));

      const req = httpTestingController.expectOne(
        `/engine-rest/task/${taskId}/comment`
      );
      expect(req.request.method).toEqual('GET');

      req.flush([]);
    });
  });

  describe('createComment', () => {
    it('should create a comment', () => {
      const taskId = '3';
      const comment = { message: 'my comment' };

      service.createComment(taskId, comment).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/task/${taskId}/comment/create`
      );
      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual(comment);

      req.flush(null);
    });
  });

  describe('getComment', () => {
    it('should get a comment', () => {
      const taskId = '3';
      const commentId = '3';

      service.getComment(taskId, commentId).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/task/${taskId}/comment/${commentId}`
      );
      expect(req.request.method).toEqual('GET');

      req.flush(null);
    });
  });

  describe('getIdentityLinks', () => {
    it('should get all identity links', () => {
      const taskId = '3';

      service
        .getIdentityLinks(taskId)
        .subscribe((data) => expect(data).toEqual([]));

      const req = httpTestingController.expectOne(
        `/engine-rest/task/${taskId}/identity-links`
      );
      expect(req.request.method).toEqual('GET');

      req.flush([]);
    });
  });

  describe('createIdentityLink', () => {
    it('should create an identity link for user identity', () => {
      const taskId = '3';
      const identity = {
        userId: 'my-user-1',
        type: 'assignee'
      };

      service.createIdentityLink(taskId, identity).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/task/${taskId}/identity-links`
      );
      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual(identity);

      req.flush(null);
    });

    it('should create an identity link for group identity', () => {
      const taskId = '3';
      const identity = {
        groupId: 'my-group-1',
        type: 'candidate'
      };

      service.createIdentityLink(taskId, identity).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/task/${taskId}/identity-links`
      );
      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual(identity);

      req.flush(null);
    });
  });

  describe('deleteIdentityLink', () => {
    it('should delete an identity link for user identity', () => {
      const taskId = '3';
      const identity = {
        userId: 'my-user-1',
        type: 'assignee'
      };

      service.deleteIdentityLink(taskId, identity).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/task/${taskId}/identity-links/delete`
      );
      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual(identity);

      req.flush(null);
    });

    it('should delete an identity link for group identity', () => {
      const taskId = '3';
      const identity = {
        groupId: 'my-group-1',
        type: 'candidate'
      };

      service.deleteIdentityLink(taskId, identity).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/task/${taskId}/identity-links/delete`
      );
      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual(identity);

      req.flush(null);
    });
  });

  describe('getLocalVariables', () => {
    it('should get all local variables', () => {
      const taskId = '3';
      const variables = {
        var1: {
          value: true
        }
      };

      service
        .getLocalVariables(taskId)
        .subscribe((data) => expect(data).toBe(variables));

      const req = httpTestingController.expectOne(
        `/engine-rest/task/${taskId}/localVariables`
      );
      expect(req.request.method).toEqual('GET');

      req.flush(variables);
    });
  });

  describe('patchLocalVariables', () => {
    it('should patch local variables', () => {
      const taskId = '3';
      const modifications = {
        var1: {
          value: true
        }
      };
      const deletions = ['var2'];

      service.patchLocalVariables(taskId, modifications, deletions).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/task/${taskId}/localVariables`
      );
      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual({ modifications, deletions });

      req.flush(null);
    });
  });

  describe('deleteLocalVariable', () => {
    it('should delete a local variable', () => {
      const taskId = '3';
      const variableName = 'var1';

      service.deleteLocalVariable(taskId, variableName).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/task/${taskId}/localVariables/${variableName}`
      );
      expect(req.request.method).toEqual('DELETE');

      req.flush(null);
    });
  });

  describe('getLocalVariable', () => {
    it('should get a local variable', () => {
      const taskId = '3';
      const variableName = 'var1';

      service.getLocalVariable(taskId, variableName).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/task/${taskId}/localVariables/${variableName}`
      );
      expect(req.request.method).toEqual('GET');

      req.flush(null);
    });
  });

  describe('setLocalVariable', () => {
    it('should set a local variable', () => {
      const taskId = '3';
      const variableName = 'var1';
      const value = {};

      service.setLocalVariable(taskId, variableName, value).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/task/${taskId}/localVariables/${variableName}`
      );
      expect(req.request.method).toEqual('PUT');
      expect(req.request.body).toEqual(value);

      req.flush(null);
    });
  });

  describe('getLocalVariableData', () => {
    it('should get data of a local variable', () => {
      const taskId = '3';
      const variableName = 'var1';

      service.getLocalVariableData(taskId, variableName).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/task/${taskId}/localVariables/${variableName}/data`
      );
      expect(req.request.method).toEqual('GET');

      req.flush(null);
    });
  });

  describe('setLocalVariableData', () => {
    it('should set data of a local variable', () => {
      const taskId = '3';
      const variableName = 'var1';
      const data = 'content';
      const type = 'mimetype';

      const formData = new FormData();
      formData.append('data', data);
      formData.append('valueType', type);

      service
        .setLocalVariableData(taskId, variableName, data, type)
        .subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/task/${taskId}/localVariables/${variableName}/data`
      );
      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual(formData);

      req.flush(null);
    });
  });

  describe('getVariables', () => {
    it('should get all variables', () => {
      const taskId = '3';
      const variables = {
        var1: {
          value: true
        }
      };

      service
        .getVariables(taskId)
        .subscribe((data) => expect(data).toBe(variables));

      const req = httpTestingController.expectOne(
        `/engine-rest/task/${taskId}/variables`
      );
      expect(req.request.method).toEqual('GET');

      req.flush(variables);
    });
  });

  describe('patchVariables', () => {
    it('should patch variables', () => {
      const taskId = '3';
      const modifications = {
        var1: {
          value: true
        }
      };
      const deletions = ['var2'];

      service.patchVariables(taskId, modifications, deletions).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/task/${taskId}/variables`
      );
      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual({ modifications, deletions });

      req.flush(null);
    });
  });

  describe('deleteVariable', () => {
    it('should delete a variable', () => {
      const taskId = '3';
      const variableName = 'var1';

      service.deleteVariable(taskId, variableName).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/task/${taskId}/variables/${variableName}`
      );
      expect(req.request.method).toEqual('DELETE');

      req.flush(null);
    });
  });

  describe('getVariable', () => {
    it('should get a variable', () => {
      const taskId = '3';
      const variableName = 'var1';

      service.getVariable(taskId, variableName).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/task/${taskId}/variables/${variableName}`
      );
      expect(req.request.method).toEqual('GET');

      req.flush(null);
    });
  });

  describe('setVariable', () => {
    it('should set a variable', () => {
      const taskId = '3';
      const variableName = 'var1';
      const value = {};

      service.setVariable(taskId, variableName, value).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/task/${taskId}/variables/${variableName}`
      );
      expect(req.request.method).toEqual('PUT');
      expect(req.request.body).toEqual(value);

      req.flush(null);
    });
  });

  describe('getVariableData', () => {
    it('should get data of a variable', () => {
      const taskId = '3';
      const variableName = 'var1';

      service.getVariableData(taskId, variableName).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/task/${taskId}/variables/${variableName}/data`
      );
      expect(req.request.method).toEqual('GET');

      req.flush(null);
    });
  });

  describe('setVariableData', () => {
    it('should set data of a variable', () => {
      const taskId = '3';
      const variableName = 'var1';
      const data = 'content';
      const type = 'mimetype';

      const formData = new FormData();
      formData.append('data', data);
      formData.append('valueType', type);

      service.setVariableData(taskId, variableName, data, type).subscribe();

      const req = httpTestingController.expectOne(
        `/engine-rest/task/${taskId}/variables/${variableName}/data`
      );
      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual(formData);

      req.flush(null);
    });
  });
});
