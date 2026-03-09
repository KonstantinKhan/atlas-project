import { z } from 'zod'

const CpmTaskDtoSchema = z.object({
	taskId: z.string(),
	es: z.string(),
	ef: z.string(),
	ls: z.string(),
	lf: z.string(),
	slack: z.number().int(),
	isCritical: z.boolean(),
})

export const CriticalPathSchema = z.object({
	tasks: z.array(CpmTaskDtoSchema),
	criticalTaskIds: z.array(z.string()),
	projectEnd: z.string(),
})

export type CriticalPath = z.infer<typeof CriticalPathSchema>
export type CpmTask = z.infer<typeof CpmTaskDtoSchema>
