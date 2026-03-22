import {
	TaskListSchema,
	TaskSchema,
	Task,
	GanttProjectPlanSchema,
	GanttProjectPlan,
	ScheduleDeltaSchema,
	ScheduleDelta,
	CriticalPathSchema,
	CriticalPath,
	BlockerChainSchema,
	BlockerChain,
	AvailableTasksSchema,
	AvailableTasks,
	WhatIfSchema,
	WhatIfResult,
} from '@/types'

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'

function projectUrl(projectId: string, path: string): string {
	return `${API_BASE_URL}/projects/${projectId}${path}`
}

export async function getProjectTasks(projectId: string): Promise<Task[]> {
	const response = await fetch(projectUrl(projectId, '/project-tasks'), {
		headers: {
			Accept: 'application/json',
		},
	})
	if (!response.ok) throw new Error('Failed to fetch project tasks')
	return TaskListSchema.parse(await response.json())
}

export async function updateProjectTask(
	projectId: string,
	id: string,
	updates: object,
): Promise<Task> {
	const res = await fetch(projectUrl(projectId, `/project-tasks/${id}`), {
		method: 'PATCH',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify(updates),
	})
	if (!res.ok) throw new Error('Failed to update project task')
	return TaskSchema.parse(await res.json())
}

export async function getProjectPlan(projectId: string): Promise<GanttProjectPlan> {
	const response = await fetch(projectUrl(projectId, '/plan'), {
		headers: {
			Accept: 'application/json',
		},
	})
	if (!response.ok) throw new Error('Failed to fetch project plan')
	return GanttProjectPlanSchema.parse(await response.json())
}

export async function changeTaskStartDate(
	projectId: string,
	planId: string,
	taskId: string,
	newPlannedStart: string,
): Promise<ScheduleDelta> {
	const res = await fetch(projectUrl(projectId, '/change-start'), {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify({ planId, taskId, newPlannedStart }),
	})
	if (!res.ok) throw new Error('Failed to change task start date')
	return ScheduleDeltaSchema.parse(await res.json())
}

export async function resizeTaskFromStart(
	projectId: string,
	planId: string,
	taskId: string,
	newPlannedStart: string,
): Promise<ScheduleDelta> {
	const res = await fetch(projectUrl(projectId, '/resize-from-start'), {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify({ planId, taskId, newPlannedStart }),
	})
	if (!res.ok) throw new Error('Failed to resize task from start')
	return ScheduleDeltaSchema.parse(await res.json())
}

export async function changeTaskEndDate(
	projectId: string,
	planId: string,
	taskId: string,
	newPlannedEnd: string,
): Promise<ScheduleDelta> {
	const res = await fetch(projectUrl(projectId, '/change-end'), {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify({ planId, taskId, newPlannedEnd }),
	})
	if (!res.ok) throw new Error('Failed to change task end date')
	return ScheduleDeltaSchema.parse(await res.json())
}

export async function createProjectTask(projectId: string, title: string): Promise<Task> {
	const response = await fetch(projectUrl(projectId, '/project-tasks/create-in-pool'), {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json',
			Accept: 'application/json',
		},
		body: JSON.stringify({ title }),
	})
	if (!response.ok) throw new Error('Failed to create project task')
	return TaskSchema.parse(await response.json())
}

export async function deleteProjectTask(projectId: string, id: string): Promise<void> {
	const res = await fetch(projectUrl(projectId, `/project-tasks/${id}`), { method: 'DELETE' })
	if (!res.ok) throw new Error('Failed to delete project task')
}

export async function assignTaskSchedule(
	projectId: string,
	taskId: string,
	start: string,
	duration: number,
): Promise<GanttProjectPlan> {
	const res = await fetch(projectUrl(projectId, `/project-tasks/${taskId}/schedule`), {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify({ start, duration }),
	})
	if (!res.ok) throw new Error('Failed to assign task schedule')
	return GanttProjectPlanSchema.parse(await res.json())
}

export async function planTaskFromEnd(
	projectId: string,
	taskId: string,
	newPlannedEnd: string,
): Promise<GanttProjectPlan> {
	const res = await fetch(projectUrl(projectId, '/plan-from-end'), {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify({ taskId, newPlannedEnd }),
	})
	if (!res.ok) throw new Error('Failed to plan task from end')
	return GanttProjectPlanSchema.parse(await res.json())
}

export async function deleteDependency(
	projectId: string,
	fromTaskId: string,
	toTaskId: string,
): Promise<GanttProjectPlan> {
	const res = await fetch(
		projectUrl(projectId, `/dependencies?from=${encodeURIComponent(fromTaskId)}&to=${encodeURIComponent(toTaskId)}`),
		{ method: 'DELETE' },
	)
	if (!res.ok) throw new Error('Failed to delete dependency')
	return GanttProjectPlanSchema.parse(await res.json())
}

export async function changeDependencyType(
	projectId: string,
	fromTaskId: string,
	toTaskId: string,
	newType: string,
): Promise<GanttProjectPlan> {
	const res = await fetch(projectUrl(projectId, '/dependencies'), {
		method: 'PATCH',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify({ fromTaskId, toTaskId, newType }),
	})
	if (!res.ok) throw new Error('Failed to change dependency type')
	return GanttProjectPlanSchema.parse(await res.json())
}

export async function getCriticalPath(projectId: string): Promise<CriticalPath> {
	const response = await fetch(projectUrl(projectId, '/critical-path'), {
		headers: { Accept: 'application/json' },
	})
	if (!response.ok) throw new Error('Failed to fetch critical path')
	return CriticalPathSchema.parse(await response.json())
}

export async function getBlockerChain(projectId: string, taskId: string): Promise<BlockerChain> {
	const response = await fetch(projectUrl(projectId, `/analysis/blocker-chain/${encodeURIComponent(taskId)}`), {
		headers: { Accept: 'application/json' },
	})
	if (!response.ok) throw new Error('Failed to fetch blocker chain')
	return BlockerChainSchema.parse(await response.json())
}

export async function getAvailableTasks(projectId: string, today: string): Promise<AvailableTasks> {
	const response = await fetch(projectUrl(projectId, `/analysis/available-tasks?today=${encodeURIComponent(today)}`), {
		headers: { Accept: 'application/json' },
	})
	if (!response.ok) throw new Error('Failed to fetch available tasks')
	return AvailableTasksSchema.parse(await response.json())
}

export async function getWhatIf(projectId: string, taskId: string, newStart: string): Promise<WhatIfResult> {
	const response = await fetch(
		projectUrl(projectId, `/analysis/what-if?taskId=${encodeURIComponent(taskId)}&newStart=${encodeURIComponent(newStart)}`),
		{ headers: { Accept: 'application/json' } },
	)
	if (!response.ok) throw new Error('Failed to fetch what-if simulation')
	return WhatIfSchema.parse(await response.json())
}

export async function getWhatIfEnd(projectId: string, taskId: string, newEnd: string): Promise<WhatIfResult> {
	const response = await fetch(
		projectUrl(projectId, `/analysis/what-if-end?taskId=${encodeURIComponent(taskId)}&newEnd=${encodeURIComponent(newEnd)}`),
		{ headers: { Accept: 'application/json' } },
	)
	if (!response.ok) throw new Error('Failed to fetch what-if-end simulation')
	return WhatIfSchema.parse(await response.json())
}

export async function reorderTasks(projectId: string, orderedIds: string[]): Promise<GanttProjectPlan> {
	const res = await fetch(projectUrl(projectId, '/reorder-tasks'), {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify({ orderedIds }),
	})
	if (!res.ok) throw new Error('Failed to reorder tasks')
	return GanttProjectPlanSchema.parse(await res.json())
}

export async function saveBaseline(projectId: string, taskIds?: string[]): Promise<void> {
	const res = await fetch(projectUrl(projectId, '/save-baseline'), {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify({ taskIds: taskIds ?? null }),
	})
	if (!res.ok) throw new Error('Failed to save baseline')
}

export async function createDependency(
	projectId: string,
	planId: string,
	fromTaskId: string,
	toTaskId: string,
	type: string = 'FS',
): Promise<GanttProjectPlan> {
	const response = await fetch(projectUrl(projectId, '/dependencies'), {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json',
			Accept: 'application/json',
		},
		body: JSON.stringify({
			planId,
			fromTaskId,
			toTaskId,
			type,
		}),
	})
	if (!response.ok) throw new Error('Failed to create dependency')
	return GanttProjectPlanSchema.parse(await response.json())
}
