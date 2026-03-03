export interface GanttDependencyDto {
  fromTaskId: string;
  toTaskId: string;
  type: DependencyTypeDto;
  lagDays?: number;
}

export enum DependencyTypeDto {
  FS = "FS",
  SS = "SS",
  FF = "FF",
  SF = "SF",
}