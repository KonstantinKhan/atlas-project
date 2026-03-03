export interface GanttProjectPlanDto {
  projectId: string;
  tasks: GanttTaskDto[];
  dependencies: GanttDependencyDto[];
}

export interface GanttTaskDto {
  id: string;
  title: string;
  start?: string | null;
  end?: string | null;
  status: ProjectTaskStatus;
}

export interface GanttDependencyDto {
  fromTaskId: string;
  toTaskId: string;
  type: DependencyTypeDto;
  lagDays?: number;
}

export enum ProjectTaskStatus {
  EMPTY = "EMPTY",
  BACKLOG = "BACKLOG",
  IN_PROGRESS = "IN_PROGRESS",
  DONE = "DONE",
  BLOCKED = "BLOCKED",
}

export enum DependencyTypeDto {
  FS = "FS",
  SS = "SS",
  FF = "FF",
  SF = "SF",
}