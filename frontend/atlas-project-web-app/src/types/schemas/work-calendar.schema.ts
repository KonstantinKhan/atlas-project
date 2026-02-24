import { z } from 'zod'

export const WorkCalendarSchema = z.object({
	weekendDays: z.array(z.number()),
	holidays: z.array(z.string()),
	workingWeekends: z
		.array(z.string())
		.nullish()
		.transform((v) => v ?? undefined),
})

export type WorkCalendar = z.infer<typeof WorkCalendarSchema>
