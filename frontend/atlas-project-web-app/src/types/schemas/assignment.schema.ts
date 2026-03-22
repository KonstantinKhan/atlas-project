import { z } from 'zod'

export const TaskAssignmentSchema = z.object({
	id: z.string(),
	taskId: z.string(),
	resourceId: z.string(),
	hoursPerDay: z.number(),
	plannedEffortHours: z.number().nullable().optional(),
})

export const AssignmentDayOverrideSchema = z.object({
	date: z.string(),
	hours: z.number(),
})

export const AssignmentDayOverrideListSchema = z.object({
	overrides: z.array(AssignmentDayOverrideSchema),
})

export const TaskAssignmentListSchema = z.object({
	assignments: z.array(TaskAssignmentSchema),
})

export const ResourceDayLoadSchema = z.object({
	date: z.string(),
	assignedHours: z.number(),
	capacityHours: z.number(),
	isOverloaded: z.boolean(),
})

export const ResourceLoadResultSchema = z.object({
	resourceId: z.string(),
	resourceName: z.string(),
	days: z.array(ResourceDayLoadSchema),
	overloadedDaysCount: z.number().int(),
	allocatedHours: z.number(),
	effortDeficit: z.number().nullable().optional(),
})

export const OverloadReportSchema = z.object({
	resources: z.array(ResourceLoadResultSchema),
	totalOverloadedDays: z.number().int(),
	totalEffortDeficit: z.number(),
})

export const ScheduleUpdateSchema = z.object({
	taskId: z.string(),
	start: z.string(),
	end: z.string(),
})

export const LevelingResultSchema = z.object({
	updatedSchedules: z.array(ScheduleUpdateSchema),
	resolvedOverloads: z.number().int(),
	remainingOverloads: z.number().int(),
})

export type TaskAssignment = z.infer<typeof TaskAssignmentSchema>
export type AssignmentDayOverride = z.infer<typeof AssignmentDayOverrideSchema>
export type ResourceDayLoad = z.infer<typeof ResourceDayLoadSchema>
export type ResourceLoadResult = z.infer<typeof ResourceLoadResultSchema>
export type OverloadReport = z.infer<typeof OverloadReportSchema>
export type ScheduleUpdate = z.infer<typeof ScheduleUpdateSchema>
export type LevelingResult = z.infer<typeof LevelingResultSchema>
