export interface TimelineCalendarDto {
  workingWeekDays: DayOfWeek[];
  weekendWeekDays: DayOfWeek[];
  holidays: string[];
  workingWeekends: string[];
}

export enum DayOfWeek {
  MONDAY = "MONDAY",
  TUESDAY = "TUESDAY",
  WEDNESDAY = "WEDNESDAY",
  THURSDAY = "THURSDAY",
  FRIDAY = "FRIDAY",
  SATURDAY = "SATURDAY",
  SUNDAY = "SUNDAY",
}