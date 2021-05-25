export interface Comment {
  id: string;
  userId: string;
  taskId: string;
  time: string;
  message: string;
  removalTime?: string;
  rootProcessInstanceId: string;
}
