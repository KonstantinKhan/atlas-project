export interface ChangeTaskEndDateCommandDto {
  planId: string;
  taskId: string;
  newPlannedEnd: string;
}