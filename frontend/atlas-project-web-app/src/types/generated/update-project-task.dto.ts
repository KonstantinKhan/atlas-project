export interface UpdateProjectTaskRequest {
  title?: string | null;
  description?: string | null;
  plannedCalendarDuration?: number | null;
  actualCalendarDuration?: number | null;
  plannedStartDate?: string | null;
  plannedEndDate?: string | null;
  actualStartDate?: string | null;
  actualEndDate?: string | null;
  status?: ProjectTaskStatus | null;
  dependsOn?: string[] | null;
  dependsOnLag?: { [key: string]: number } | null;
}

export enum ProjectTaskStatus {
  EMPTY = "EMPTY",
  BACKLOG = "BACKLOG",
  IN_PROGRESS = "IN_PROGRESS",
  DONE = "DONE",
  BLOCKED = "BLOCKED",
}