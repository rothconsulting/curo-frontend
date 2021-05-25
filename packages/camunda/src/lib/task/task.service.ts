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

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/task/get-query/
   */
  getTasks(maxResult?: number, firstResult?: number): Observable<Task[]> {
    const params = this.createPagingQueryParams(maxResult, firstResult);
    return this.httpClient.get<Task[]>(`${this.basePath}/task`, {
      params
    });
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/task/post-query/
   */
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

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/task/get-query-count/
   */
  getTasksCount(): Observable<CountResult> {
    return this.httpClient.get<CountResult>(`${this.basePath}/task/count`);
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/task/post-query-count/
   */
  queryTasksCount(query?: TaskQuery): Observable<CountResult> {
    return this.httpClient.post<CountResult>(
      `${this.basePath}/task/count`,
      query
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/task/post-create/
   */
  createTask(task: Task): Observable<void> {
    return this.httpClient.post<void>(`${this.basePath}/task/create`, task);
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/task/delete/
   */
  deleteTask(id: string): Observable<void> {
    return this.httpClient.delete<void>(`${this.basePath}/task/${id}`);
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/task/get/
   */
  getTask(id: string): Observable<Task> {
    return this.httpClient.get<Task>(`${this.basePath}/task/${id}`);
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/task/put-update/
   */
  updateTask(id: string, task: Task): Observable<void> {
    return this.httpClient.put<void>(`${this.basePath}/task/${id}`, task);
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/task/post-assignee/
   */
  assignTask(id: string, userId: string): Observable<void> {
    return this.httpClient.post<void>(`${this.basePath}/task/${id}/assignee`, {
      userId
    });
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/task/post-claim/
   */
  claimTask(id: string, userId: string): Observable<void> {
    return this.httpClient.post<void>(`${this.basePath}/task/${id}/claim`, {
      userId
    });
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/task/post-complete/
   */
  completeTask(id: string, variables: Variables): Observable<void> {
    return this.httpClient.post<void>(`${this.basePath}/task/${id}/complete`, {
      variables
    });
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/task/post-delegate/
   */
  delegateTask(id: string, userId: string): Observable<void> {
    return this.httpClient.post<void>(`${this.basePath}/task/${id}/delegate`, {
      userId
    });
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/task/get-form-key/
   */
  getForm(id: string): Observable<Form> {
    return this.httpClient.get<Form>(`${this.basePath}/task/${id}/form`);
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/task/get-form-variables/
   */
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

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/task/post-resolve/
   */
  resolveTask(id: string, variables: Variables): Observable<void> {
    return this.httpClient.post<void>(`${this.basePath}/task/${id}/resolve`, {
      variables
    });
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/task/post-unclaim/
   */
  unclaimTask(id: string): Observable<void> {
    return this.httpClient.post<void>(
      `${this.basePath}/task/${id}/unclaim`,
      null
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/task/attachment/get-task-attachments/
   */
  getAttachments(id: string): Observable<Attachment[]> {
    return this.httpClient.get<Attachment[]>(
      `${this.basePath}/task/${id}/attachment`
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/task/attachment/post-task-attachment/
   */
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

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/task/attachment/delete-task-attachment/
   */
  deleteAttachment(id: string, attachmentId: string): Observable<void> {
    return this.httpClient.delete<void>(
      `${this.basePath}/task/${id}/attachment/${attachmentId}`
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/task/attachment/get-task-attachment/
   */
  getAttachment(id: string, attachmentId: string): Observable<Attachment> {
    return this.httpClient.get<Attachment>(
      `${this.basePath}/task/${id}/attachment/${attachmentId}`
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/task/attachment/get-task-attachment-data/
   */
  getAttachmentData(id: string, attachmentId: string): Observable<string> {
    return this.httpClient.get<string>(
      `${this.basePath}/task/${id}/attachment/${attachmentId}/data`
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/task/comment/get-task-comments/
   */
  getComments(id: string): Observable<Comment[]> {
    return this.httpClient.get<Comment[]>(
      `${this.basePath}/task/${id}/comment`
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/task/comment/post-task-comment/
   */
  createComment(id: string, comment: Partial<Comment>): Observable<Comment> {
    return this.httpClient.post<Comment>(
      `${this.basePath}/task/${id}/comment/create`,
      comment
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/task/comment/get-task-comment/
   */
  getComment(id: string, commentId: string): Observable<Comment> {
    return this.httpClient.get<Comment>(
      `${this.basePath}/task/${id}/comment/${commentId}`
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/task/identity-links/get-identity-links/
   */
  getIdentityLinks(id: string): Observable<IdentityLink[]> {
    return this.httpClient.get<IdentityLink[]>(
      `${this.basePath}/task/${id}/identity-links`
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/task/identity-links/post-identity-link/
   */
  createIdentityLink(id: string, identityLink: IdentityLink): Observable<void> {
    return this.httpClient.post<void>(
      `${this.basePath}/task/${id}/identity-links`,
      identityLink
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/task/identity-links/post-delete-identity-link/
   */
  deleteIdentityLink(id: string, identityLink: IdentityLink): Observable<void> {
    return this.httpClient.post<void>(
      `${this.basePath}/task/${id}/identity-links/delete`,
      identityLink
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/task/local-variables/get-local-task-variables/
   */
  getLocalVariables(id: string): Observable<Variables> {
    return this.httpClient.get<Variables>(
      `${this.basePath}/task/${id}/localVariables`
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/task/local-variables/post-modify-local-task-variables/
   */
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

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/task/local-variables/delete-local-task-variable/
   */
  deleteLocalVariable(id: string, variableName: string): Observable<void> {
    return this.httpClient.delete<void>(
      `${this.basePath}/task/${id}/localVariables/${variableName}`
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/task/local-variables/get-local-task-variable/
   */
  getLocalVariable<T>(
    id: string,
    variableName: string
  ): Observable<VariableValue<T>> {
    return this.httpClient.get<VariableValue<T>>(
      `${this.basePath}/task/${id}/localVariables/${variableName}`
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/task/local-variables/put-local-task-variable/
   */
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

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/task/local-variables/get-local-task-variable-binary/
   */
  getLocalVariableData(id: string, variableName: string): Observable<string> {
    return this.httpClient.get<string>(
      `${this.basePath}/task/${id}/localVariables/${variableName}/data`
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/task/local-variables/post-local-task-variable-binary/
   */
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

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/task/variables/get-task-variables/
   */
  getVariables(id: string): Observable<Variables> {
    return this.httpClient.get<Variables>(
      `${this.basePath}/task/${id}/variables`
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/task/variables/post-modify-task-variables/
   */
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

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/task/variables/delete-task-variable/
   */
  deleteVariable(id: string, variableName: string): Observable<void> {
    return this.httpClient.delete<void>(
      `${this.basePath}/task/${id}/variables/${variableName}`
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/task/variables/get-task-variable/
   */
  getVariable<T>(
    id: string,
    variableName: string
  ): Observable<VariableValue<T>> {
    return this.httpClient.get<VariableValue<T>>(
      `${this.basePath}/task/${id}/variables/${variableName}`
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/task/variables/put-task-variable/
   */
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

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/task/variables/get-task-variable-binary/
   */
  getVariableData(id: string, variableName: string): Observable<string> {
    return this.httpClient.get<string>(
      `${this.basePath}/task/${id}/variables/${variableName}/data`
    );
  }

  /**
   * @see https://docs.camunda.org/manual/7.14/reference/rest/task/variables/post-task-variable-binary/
   */
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
