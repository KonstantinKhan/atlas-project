import { z } from 'zod'

export const ProjectContributionSchema = z.object({
	projectId: z.string(),
	projectName: z.string(),
	hours: z.number(),
})

export const CrossProjectDayLoadSchema = z.object({
	date: z.string(),
	totalAssignedHours: z.number(),
	capacityHours: z.number(),
	isOverloaded: z.boolean(),
	projectBreakdown: z.array(ProjectContributionSchema),
})

export const CrossProjectResourceLoadSchema = z.object({
	resourceId: z.string(),
	resourceName: z.string(),
	days: z.array(CrossProjectDayLoadSchema),
	overloadedDaysCount: z.number().int(),
	totalAllocatedHours: z.number(),
})

export const ProjectInfoSchema = z.object({
	id: z.string(),
	name: z.string(),
	priority: z.number().int(),
	portfolioId: z.string(),
})

export const CrossProjectOverloadReportSchema = z.object({
	resources: z.array(CrossProjectResourceLoadSchema),
	projects: z.array(ProjectInfoSchema),
	totalOverloadedDays: z.number().int(),
})

export type ProjectContribution = z.infer<typeof ProjectContributionSchema>
export type CrossProjectDayLoad = z.infer<typeof CrossProjectDayLoadSchema>
export type CrossProjectResourceLoad = z.infer<typeof CrossProjectResourceLoadSchema>
export type ProjectInfo = z.infer<typeof ProjectInfoSchema>
export type CrossProjectOverloadReport = z.infer<typeof CrossProjectOverloadReportSchema>
