export interface CreateProjectTaskRequest {
  title: string;
  description?: string;
  plannedCalendarDuration?: number | null;
  actualCalendarDuration?: number | null;
  plannedStartDate?: string | null;
  plannedEndDate?: string | null;
  actualStartDate?: string | null;
  actualEndDate?: string | null;
  status?: ProjectTaskStatus;
  dependsOn?: string[];
  dependsOnLag?: { [key: string]: number };
}

export enum ProjectTaskStatus {
  EMPTY = "EMPTY",
  BACKLOG = "BACKLOG",
  IN_PROGRESS = "IN_PROGRESS",
  DONE = "DONE",
  BLOCKED = "BLOCKED",
}