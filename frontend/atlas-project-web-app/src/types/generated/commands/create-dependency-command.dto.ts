export interface CreateDependencyCommandDto {
  planId: string;
  fromTaskId: string;
  toTaskId: string;
  type?: DependencyTypeDto;
  lagDays?: number | null;
}

export enum DependencyTypeDto {
  FS = "FS",
  SS = "SS",
  FF = "FF",
  SF = "SF",
}