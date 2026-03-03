import { z } from 'zod'
import { ProjectTaskStatus } from '@/types/generated/enums/project-task-status.enum'

const optionalDate = z
	.string()
	.nullable()
	.transform((v) => (v ? new Date(v) : undefined))

const optionalInt = z
	.number()
	.int()
	.nullable()
	.transform((v) => v ?? undefined)

const projectTaskStatus = z.enum(ProjectTaskStatus)

export const TaskSchema = z.object({
	id: z.string(),
	title: z.string(),
	description: z.string(),
	status: projectTaskStatus,
})

export const ScheduledTaskSchema = TaskSchema.extend({
	plannedCalendarDuration: optionalInt,
	plannedStartDate: optionalDate,
	plannedEndDate: optionalDate,
	dependsOn: z.array(z.string()).default([]),
	dependsOnLag: z.record(z.string(), z.number()).default({}),
})

export const GanttTaskSchema = z.object({
	id: z.string(),
	title: z.string(),
	start: z.string().transform((v) => new Date(v)).nullable().optional(),
	end: z.string().transform((v) => new Date(v)).nullable().optional(),
	status: projectTaskStatus,
})

export const TaskListSchema = z.array(TaskSchema)
export const ScheduledTaskListSchema = z.array(ScheduledTaskSchema)
export const GanttTaskListSchema = z.array(GanttTaskSchema)

export type Task = z.infer<typeof TaskSchema>
export type ScheduledTask = z.infer<typeof ScheduledTaskSchema>
export type GanttTask = z.infer<typeof GanttTaskSchema>
