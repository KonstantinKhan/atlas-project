import {
	TaskListSchema,
	TaskSchema,
	Task,
	GanttProjectPlanSchema,
	GanttProjectPlan,
	ScheduleDeltaSchema,
	ScheduleDelta,
} from '@/types'

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'

export async function getProjectTasks(): Promise<Task[]> {
	const response = await fetch(`${API_BASE_URL}/project-tasks`, {
		headers: {
			Accept: 'application/json',
		},
	})
	if (!response.ok) throw new Error('Failed to fetch project tasks')
	return TaskListSchema.parse(await response.json())
}

export async function updateProjectTask(
	id: string,
	updates: object,
): Promise<Task> {
	const res = await fetch(`${API_BASE_URL}/project-tasks/${id}`, {
		method: 'PATCH',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify(updates),
	})
	if (!res.ok) throw new Error('Failed to update project task')
	return TaskSchema.parse(await res.json())
}

export async function getProjectPlan(): Promise<GanttProjectPlan> {
	const response = await fetch(`${API_BASE_URL}/project-plan`, {
		headers: {
			Accept: 'application/json',
		},
	})
	if (!response.ok) throw new Error('Failed to fetch project plan')
	return GanttProjectPlanSchema.parse(await response.json())
}

export async function changeTaskStartDate(
	planId: string,
	taskId: string,
	newPlannedStart: string,
): Promise<ScheduleDelta> {
	const res = await fetch(`${API_BASE_URL}/change-start`, {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify({ planId, taskId, newPlannedStart }),
	})
	if (!res.ok) throw new Error('Failed to change task start date')
	return ScheduleDeltaSchema.parse(await res.json())
}

export async function changeTaskEndDate(
	planId: string,
	taskId: string,
	newPlannedEnd: string,
): Promise<ScheduleDelta> {
	const res = await fetch(`${API_BASE_URL}/change-end`, {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify({ planId, taskId, newPlannedEnd }),
	})
	if (!res.ok) throw new Error('Failed to change task end date')
	return ScheduleDeltaSchema.parse(await res.json())
}

export async function createProjectTask(): Promise<Task> {
	const response = await fetch(`${API_BASE_URL}/project-tasks`, {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json',
			Accept: 'application/json',
		},
		body: JSON.stringify({ title: '' }),
	})
	if (!response.ok) throw new Error('Failed to create project task')
	return TaskSchema.parse(await response.json())
}

export async function createDependency(
	planId: string,
	fromTaskId: string,
	toTaskId: string,
	type: string = 'FS',
): Promise<GanttProjectPlan> {
	const response = await fetch(`${API_BASE_URL}/dependencies`, {
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
