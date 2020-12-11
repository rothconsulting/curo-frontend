export interface Attachment {
  id: string;
  name: string;
  description: string;
  taskId: string;
  type: string;
  url: string;
  createTime: string;
  removalTime?: string;
  rootProcessInstanceId: string;
}
