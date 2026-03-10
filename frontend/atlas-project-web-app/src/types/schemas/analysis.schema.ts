import { z } from 'zod'

const BlockerInfoSchema = z.object({
	taskId: z.string(),
	title: z.string(),
	status: z.string(),
	start: z.string().nullable().optional(),
	end: z.string().nullable().optional(),
	depth: z.number().int(),
})

export const BlockerChainSchema = z.object({
	targetTaskId: z.string(),
	blockers: z.array(BlockerInfoSchema),
})

export type BlockerChain = z.infer<typeof BlockerChainSchema>
export type BlockerInfo = z.infer<typeof BlockerInfoSchema>

const AvailableTaskItemSchema = z.object({
	taskId: z.string(),
	title: z.string(),
	status: z.string(),
	start: z.string(),
	end: z.string(),
})

export const AvailableTasksSchema = z.object({
	tasks: z.array(AvailableTaskItemSchema),
	asOfDate: z.string(),
})

export type AvailableTasks = z.infer<typeof AvailableTasksSchema>

const TaskImpactSchema = z.object({
	taskId: z.string(),
	title: z.string(),
	oldStart: z.string(),
	oldEnd: z.string(),
	newStart: z.string(),
	newEnd: z.string(),
	deltaStartDays: z.number().int(),
	deltaEndDays: z.number().int(),
})

export const WhatIfSchema = z.object({
	movedTaskId: z.string(),
	impacts: z.array(TaskImpactSchema),
})

export type WhatIfResult = z.infer<typeof WhatIfSchema>
