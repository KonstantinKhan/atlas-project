export interface ScheduleDeltaDto {
  updatedSchedules: ScheduleUpdateDto[];
}

export interface ScheduleUpdateDto {
  taskId: string;
  start: string;
  end: string;
}