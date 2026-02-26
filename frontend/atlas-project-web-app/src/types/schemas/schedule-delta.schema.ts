import { z } from 'zod'

export const ScheduleUpdateSchema = z.object({
	taskId: z.string(),
	start: z.string().transform((v) => new Date(v)),
	end: z.string().transform((v) => new Date(v)),
})

export const ScheduleDeltaSchema = z.object({
	updatedSchedules: z.array(ScheduleUpdateSchema),
})

export type ScheduleDelta = z.infer<typeof ScheduleDeltaSchema>
