export interface GanttTaskDto {
  id: string;
  title: string;
  start?: string | null;
  end?: string | null;
  status: ProjectTaskStatus;
}

export enum ProjectTaskStatus {
  EMPTY = "EMPTY",
  BACKLOG = "BACKLOG",
  IN_PROGRESS = "IN_PROGRESS",
  DONE = "DONE",
  BLOCKED = "BLOCKED",
}