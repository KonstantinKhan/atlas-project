export interface ScheduledTaskDto {
  id: string;
  title: string;
  description?: string;
  plannedStartDate: string;
  plannedEndDate: string;
  plannedCalendarDuration?: number | null;
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