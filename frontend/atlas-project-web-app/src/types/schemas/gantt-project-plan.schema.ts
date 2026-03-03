import { z } from 'zod'
import { ProjectTaskStatus } from '@/types/generated/enums/project-task-status.enum'
import { GanttTaskSchema } from './task.schema'

const projectTaskStatus = z.nativeEnum(ProjectTaskStatus)

const GanttDependencyDtoSchema = z.object({
	fromTaskId: z.string(),
	toTaskId: z.string(),
	type: z.string(),
	lagDays: z.number().int().default(0),
})

export const GanttProjectPlanSchema = z.object({
	projectId: z.string(),
	tasks: z.array(GanttTaskSchema),
	dependencies: z.array(GanttDependencyDtoSchema),
})

export type GanttProjectPlan = z.infer<typeof GanttProjectPlanSchema>
export type GanttDependencyDto = z.infer<typeof GanttDependencyDtoSchema>
export type GanttTask = z.infer<typeof GanttTaskSchema>
