import { z } from 'zod'

export const PortfolioSchema = z.object({
	id: z.string().nullish(),
	name: z.string().nullish(),
	description: z.string().nullish(),
})

export const PortfolioListSchema = z.object({
	foundPortfolios: z.array(PortfolioSchema),
})

export const ProjectPrioritySchema = z.enum(['HIGH', 'MEDIUM', 'LOW'])
export type ProjectPriority = z.infer<typeof ProjectPrioritySchema>

export const ProjectSummarySchema = z.object({
	id: z.string(),
	name: z.string(),
	priority: ProjectPrioritySchema,
	taskCount: z.number(),
})

export const ProjectSummaryListSchema = z.object({
	projects: z.array(ProjectSummarySchema),
})

export type Portfolio = z.infer<typeof PortfolioSchema>
export type ProjectSummary = z.infer<typeof ProjectSummarySchema>
