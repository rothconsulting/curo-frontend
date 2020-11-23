import { HttpClient, HttpParams } from '@angular/common/http';
import { Inject, Injectable, Optional } from '@angular/core';
import { Observable } from 'rxjs';
import { Attachment } from '../attachment';
import { CAMUNDA_BASE_PATH } from '../camunda-base-path';
import { Comment } from '../comment';
import { CountResult } from '../count-result';
import { Form } from '../form';
import { IdentityLink } from '../identity-link';
import { Task } from '../task';
import { TaskQuery } from '../task-query';
import { VariableValue } from '../variable-value';
import { Variables } from '../variables';

@Injectable({
  providedIn: 'root'
})
export class TaskService {
  basePath: string;

  constructor(
    @Optional() @Inject(CAMUNDA_BASE_PATH) camundaBasePath: string,
    private httpClient: HttpClient
  ) {
    this.basePath = camundaBasePath || '';
  }

  getTasks(maxResult?: number, firstResult?: number): Observable<Task[]> {
    const params = this.createPagingQueryParams(maxResult, firstResult);
    return this.httpClient.get<Task[]>(`${this.basePath}/task`, {
      params
    });
  }

  queryTasks(
    query?: TaskQuery,
    maxResult?: number,
    firstResult?: number
  ): Observable<Task[]> {
    const params = this.createPagingQueryParams(maxResult, firstResult);
    return this.httpClient.post<Task[]>(`${this.basePath}/task`, query, {
      params
    });
  }

  getTasksCount(): Observable<CountResult> {
    return this.httpClient.get<CountResult>(`${this.basePath}/task/count`);
  }

  queryTasksCount(query?: TaskQuery): Observable<CountResult> {
    return this.httpClient.post<CountResult>(
      `${this.basePath}/task/count`,
      query
    );
  }

  createTask(task: Task): Observable<void> {
    return this.httpClient.post<void>(`${this.basePath}/task/create`, task);
  }

  deleteTask(id: string): Observable<void> {
    return this.httpClient.delete<void>(`${this.basePath}/task/${id}`);
  }

  getTask(id: string): Observable<Task> {
    return this.httpClient.get<Task>(`${this.basePath}/task/${id}`);
  }

  updateTask(id: string, task: Task): Observable<void> {
    return this.httpClient.put<void>(`${this.basePath}/task/${id}`, task);
  }

  assignTask(id: string, userId: string): Observable<void> {
    return this.httpClient.post<void>(`${this.basePath}/task/${id}/assignee`, {
      userId
    });
  }

  claimTask(id: string, userId: string): Observable<void> {
    return this.httpClient.post<void>(`${this.basePath}/task/${id}/claim`, {
      userId
    });
  }

  completeTask(id: string, variables: Variables): Observable<void> {
    return this.httpClient.post<void>(`${this.basePath}/task/${id}/complete`, {
      variables
    });
  }

  delegateTask(id: string, userId: string): Observable<void> {
    return this.httpClient.post<void>(`${this.basePath}/task/${id}/delegate`, {
      userId
    });
  }

  getForm(id: string): Observable<Form> {
    return this.httpClient.get<Form>(`${this.basePath}/task/${id}/form`);
  }

  getFormVariables(
    id: string,
    variableNames?: string[]
  ): Observable<Variables> {
    let params;
    if (variableNames) {
      params = { variableNames: variableNames.join(',') };
    }

    return this.httpClient.get<Variables>(
      `${this.basePath}/task/${id}/form-variables`,
      {
        params
      }
    );
  }

  resolveTask(id: string, variables: Variables): Observable<void> {
    return this.httpClient.post<void>(`${this.basePath}/task/${id}/resolve`, {
      variables
    });
  }

  unclaimTask(id: string): Observable<void> {
    return this.httpClient.post<void>(
      `${this.basePath}/task/${id}/unclaim`,
      null
    );
  }

  getAttachments(id: string): Observable<Attachment[]> {
    return this.httpClient.get<Attachment[]>(
      `${this.basePath}/task/${id}/attachment`
    );
  }

  createAttachment(
    id: string,
    name: string,
    description: string,
    type: string,
    url: string,
    content: string
  ): Observable<Attachment> {
    const formData = new FormData();
    formData.append('attachment-name', name);
    formData.append('attachment-description', description);
    formData.append('attachment-type', type);
    formData.append('url', url);
    formData.append('content', content);

    return this.httpClient.post<Attachment>(
      `${this.basePath}/task/${id}/attachment/create`,
      formData
    );
  }

  deleteAttachment(id: string, attachmentId: string): Observable<void> {
    return this.httpClient.delete<void>(
      `${this.basePath}/task/${id}/attachment/${attachmentId}`
    );
  }

  getAttachment(id: string, attachmentId: string): Observable<Attachment> {
    return this.httpClient.get<Attachment>(
      `${this.basePath}/task/${id}/attachment/${attachmentId}`
    );
  }

  getAttachmentData(id: string, attachmentId: string): Observable<string> {
    return this.httpClient.get<string>(
      `${this.basePath}/task/${id}/attachment/${attachmentId}/data`
    );
  }

  getComments(id: string): Observable<Comment[]> {
    return this.httpClient.get<Comment[]>(
      `${this.basePath}/task/${id}/comment`
    );
  }

  createComment(id: string, comment: Partial<Comment>): Observable<Comment> {
    return this.httpClient.post<Comment>(
      `${this.basePath}/task/${id}/comment/create`,
      comment
    );
  }

  getComment(id: string, commentId: string): Observable<Comment> {
    return this.httpClient.get<Comment>(
      `${this.basePath}/task/${id}/comment/${commentId}`
    );
  }

  getIdentityLinks(id: string): Observable<IdentityLink[]> {
    return this.httpClient.get<IdentityLink[]>(
      `${this.basePath}/task/${id}/identity-links`
    );
  }

  createIdentityLink(id: string, identityLink: IdentityLink): Observable<void> {
    return this.httpClient.post<void>(
      `${this.basePath}/task/${id}/identity-links`,
      identityLink
    );
  }

  deleteIdentityLink(id: string, identityLink: IdentityLink): Observable<void> {
    return this.httpClient.post<void>(
      `${this.basePath}/task/${id}/identity-links/delete`,
      identityLink
    );
  }

  getLocalVariables(id: string): Observable<Variables> {
    return this.httpClient.get<Variables>(
      `${this.basePath}/task/${id}/localVariables`
    );
  }

  patchLocalVariables(
    id: string,
    modifications: Variables,
    deletions: string[]
  ): Observable<void> {
    return this.httpClient.post<void>(
      `${this.basePath}/task/${id}/localVariables`,
      {
        modifications,
        deletions
      }
    );
  }

  deleteLocalVariable(id: string, variableName: string): Observable<void> {
    return this.httpClient.delete<void>(
      `${this.basePath}/task/${id}/localVariables/${variableName}`
    );
  }

  getLocalVariable<T>(
    id: string,
    variableName: string
  ): Observable<VariableValue<T>> {
    return this.httpClient.get<VariableValue<T>>(
      `${this.basePath}/task/${id}/localVariables/${variableName}`
    );
  }

  setLocalVariable(
    id: string,
    variableName: string,
    value: VariableValue<unknown>
  ): Observable<void> {
    return this.httpClient.put<void>(
      `${this.basePath}/task/${id}/localVariables/${variableName}`,
      value
    );
  }

  getLocalVariableData(id: string, variableName: string): Observable<string> {
    return this.httpClient.get<string>(
      `${this.basePath}/task/${id}/localVariables/${variableName}/data`
    );
  }

  setLocalVariableData(
    id: string,
    variableName: string,
    data: string,
    type: string
  ): Observable<void> {
    const formData = new FormData();
    formData.append('data', data);
    formData.append('valueType', type);

    return this.httpClient.post<void>(
      `${this.basePath}/task/${id}/localVariables/${variableName}/data`,
      formData
    );
  }

  getVariables(id: string): Observable<Variables> {
    return this.httpClient.get<Variables>(
      `${this.basePath}/task/${id}/variables`
    );
  }

  patchVariables(
    id: string,
    modifications: Variables,
    deletions: string[]
  ): Observable<void> {
    return this.httpClient.post<void>(`${this.basePath}/task/${id}/variables`, {
      modifications,
      deletions
    });
  }

  deleteVariable(id: string, variableName: string): Observable<void> {
    return this.httpClient.delete<void>(
      `${this.basePath}/task/${id}/variables/${variableName}`
    );
  }

  getVariable<T>(
    id: string,
    variableName: string
  ): Observable<VariableValue<T>> {
    return this.httpClient.get<VariableValue<T>>(
      `${this.basePath}/task/${id}/variables/${variableName}`
    );
  }

  setVariable(
    id: string,
    variableName: string,
    value: VariableValue<unknown>
  ): Observable<void> {
    return this.httpClient.put<void>(
      `${this.basePath}/task/${id}/variables/${variableName}`,
      value
    );
  }

  getVariableData(id: string, variableName: string): Observable<string> {
    return this.httpClient.get<string>(
      `${this.basePath}/task/${id}/variables/${variableName}/data`
    );
  }

  setVariableData(
    id: string,
    variableName: string,
    data: string,
    type: string
  ): Observable<void> {
    const formData = new FormData();
    formData.append('data', data);
    formData.append('valueType', type);

    return this.httpClient.post<void>(
      `${this.basePath}/task/${id}/variables/${variableName}/data`,
      formData
    );
  }

  private createPagingQueryParams(
    maxResult?: number,
    firstResult?: number
  ): HttpParams {
    let params = new HttpParams();

    if (maxResult) {
      params = params.append('maxResult', `${maxResult}`);
    }

    if (firstResult) {
      params = params.append('firstResult', `${firstResult}`);
    }

    return params;
  }
}
