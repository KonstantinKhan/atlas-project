import { z } from 'zod'
import { TaskStatus } from '@/types/enums/task-status.enum'

const optionalDate = z
	.string()
	.nullable()
	.transform((v) => (v ? new Date(v) : undefined))

const optionalInt = z
	.number()
	.int()
	.nullable()
	.transform((v) => v ?? undefined)

const serverStatusMap: Record<string, TaskStatus> = {
	EMPTY: TaskStatus.EMPTY,
	BACKLOG: TaskStatus.BACKLOG,
	IN_PROGRESS: TaskStatus.IN_PROGRESS,
	DONE: TaskStatus.DONE,
	BLOCKED: TaskStatus.BLOCKED,
}

const projectTaskStatus = z
	.string()
	.transform((v, ctx) => {
		const mapped = serverStatusMap[v]
		if (mapped === undefined) {
			ctx.addIssue({ code: 'custom', message: `Unknown status: ${v}` })
			return z.NEVER
		}
		return mapped
	})

export const TaskSchema = z.object({
	id: z.string(),
	title: z.string(),
	description: z.string(),
	plannedCalendarDuration: optionalInt,
	actualCalendarDuration: optionalInt,
	plannedStartDate: optionalDate,
	plannedEndDate: optionalDate,
	actualStartDate: optionalDate,
	actualEndDate: optionalDate,
	status: projectTaskStatus,
	dependsOn: z.array(z.string()).default([]),
	dependsOnLag: z.record(z.string(), z.number()).default({}),
})

export const TaskListSchema = z.array(TaskSchema)

export type Task = z.infer<typeof TaskSchema>
