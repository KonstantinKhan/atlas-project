export interface ChangeTaskStartDateCommandDto {
  planId: string;
  taskId: string;
  newPlannedStart: string;
}