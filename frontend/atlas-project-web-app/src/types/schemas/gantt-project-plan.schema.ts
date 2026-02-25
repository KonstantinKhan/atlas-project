import { z } from 'zod'
import { TaskStatus } from '@/types/enums/task-status.enum'

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

const GanttTaskDtoSchema = z.object({
	id: z.string(),
	title: z.string(),
	start: z.string().transform((v) => new Date(v)),
	end: z.string().transform((v) => new Date(v)),
	status: projectTaskStatus,
})

const GanttDependencyDtoSchema = z.object({
	fromTaskId: z.string(),
	toTaskId: z.string(),
	type: z.string(),
	lagDays: z.number().int().default(0),
})

export const GanttProjectPlanSchema = z.object({
	projectId: z.string(),
	tasks: z.array(GanttTaskDtoSchema),
	dependencies: z.array(GanttDependencyDtoSchema),
})

export type GanttProjectPlan = z.infer<typeof GanttProjectPlanSchema>
export type GanttTaskDto = z.infer<typeof GanttTaskDtoSchema>
export type GanttDependencyDto = z.infer<typeof GanttDependencyDtoSchema>
