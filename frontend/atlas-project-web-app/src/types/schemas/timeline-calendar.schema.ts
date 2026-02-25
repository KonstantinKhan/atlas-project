import { z } from 'zod'

export const DayOfWeekSchema = z.enum([
	'SUNDAY',
	'MONDAY',
	'TUESDAY',
	'WEDNESDAY',
	'THURSDAY',
	'FRIDAY',
	'SATURDAY',
])

export const TimelineCalendarSchema = z.object({
	workingWeekDays: z.array(DayOfWeekSchema).default([
		'MONDAY',
		'TUESDAY',
		'WEDNESDAY',
		'THURSDAY',
		'FRIDAY',
	]),
	weekendWeekDays: z.array(DayOfWeekSchema).default(['SATURDAY', 'SUNDAY']),
	holidays: z.array(z.string()).default([]),
	workingWeekends: z.array(z.string()).default([]),
})

export type TimelineCalendar = z.infer<typeof TimelineCalendarSchema>
export type DayOfWeek = z.infer<typeof DayOfWeekSchema>

export function createDefaultTimelineCalendar(): TimelineCalendar {
	return {
		workingWeekDays: ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'],
		weekendWeekDays: ['SATURDAY', 'SUNDAY'],
		holidays: [],
		workingWeekends: [],
	}
}
