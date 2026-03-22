import { z } from 'zod'

export const PortfolioSchema = z.object({
	id: z.string(),
	name: z.string(),
	description: z.string(),
})

export const PortfolioListSchema = z.object({
	portfolios: z.array(PortfolioSchema),
})

export const ProjectSummarySchema = z.object({
	id: z.string(),
	name: z.string(),
	priority: z.number(),
	taskCount: z.number(),
})

export const ProjectSummaryListSchema = z.object({
	projects: z.array(ProjectSummarySchema),
})

export type Portfolio = z.infer<typeof PortfolioSchema>
export type ProjectSummary = z.infer<typeof ProjectSummarySchema>
