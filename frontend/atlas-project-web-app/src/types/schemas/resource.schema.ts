import { z } from 'zod'

export const ResourceSchema = z.object({
	id: z.string(),
	name: z.string(),
	type: z.enum(['PERSON', 'ROLE']),
	capacityHoursPerDay: z.number(),
	sortOrder: z.number().int().default(0),
})

export const ResourceListSchema = z.object({
	resources: z.array(ResourceSchema),
})

export const ResourceCalendarOverrideSchema = z.object({
	date: z.string(),
	availableHours: z.number(),
})

export const ResourceCalendarOverrideListSchema = z.object({
	overrides: z.array(ResourceCalendarOverrideSchema),
})

export type Resource = z.infer<typeof ResourceSchema>
export type ResourceType = Resource['type']
export type ResourceCalendarOverride = z.infer<typeof ResourceCalendarOverrideSchema>
