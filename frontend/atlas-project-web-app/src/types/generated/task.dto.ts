export interface TaskDto {
  id: string;
  title: string;
  description?: string;
  status?: ProjectTaskStatus;
}

export enum ProjectTaskStatus {
  EMPTY = "EMPTY",
  BACKLOG = "BACKLOG",
  IN_PROGRESS = "IN_PROGRESS",
  DONE = "DONE",
  BLOCKED = "BLOCKED",
}