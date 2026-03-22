import {
	TaskAssignmentSchema,
	TaskAssignmentListSchema,
	AssignmentDayOverrideSchema,
	AssignmentDayOverrideListSchema,
	OverloadReportSchema,
	ResourceLoadResultSchema,
	LevelingResultSchema,
	type TaskAssignment,
	type AssignmentDayOverride,
	type OverloadReport,
	type ResourceLoadResult,
	type LevelingResult,
} from '@/types/schemas/assignment.schema'

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'

function projectUrl(projectId: string, path: string): string {
	return `${API_BASE_URL}/projects/${projectId}${path}`
}

export async function getAssignments(projectId: string): Promise<TaskAssignment[]> {
	const response = await fetch(projectUrl(projectId, '/assignments'), {
		headers: { Accept: 'application/json' },
	})
	if (!response.ok) throw new Error('Failed to fetch assignments')
	const data = TaskAssignmentListSchema.parse(await response.json())
	return data.assignments
}

export async function createAssignment(
	projectId: string,
	taskId: string,
	resourceId: string,
	hoursPerDay: number = 8.0,
	plannedEffortHours?: number | null,
): Promise<TaskAssignment> {
	const response = await fetch(projectUrl(projectId, '/assignments'), {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify({ taskId, resourceId, hoursPerDay, plannedEffortHours }),
	})
	if (!response.ok) throw new Error('Failed to create assignment')
	return TaskAssignmentSchema.parse(await response.json())
}

export async function updateAssignment(
	projectId: string,
	id: string,
	updates: { hoursPerDay?: number; plannedEffortHours?: number | null },
): Promise<TaskAssignment> {
	const response = await fetch(projectUrl(projectId, `/assignments/${id}`), {
		method: 'PATCH',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify(updates),
	})
	if (!response.ok) throw new Error('Failed to update assignment')
	return TaskAssignmentSchema.parse(await response.json())
}

export async function getDayOverrides(projectId: string, assignmentId: string): Promise<AssignmentDayOverride[]> {
	const response = await fetch(projectUrl(projectId, `/assignments/${assignmentId}/day-overrides`), {
		headers: { Accept: 'application/json' },
	})
	if (!response.ok) throw new Error('Failed to fetch day overrides')
	const data = AssignmentDayOverrideListSchema.parse(await response.json())
	return data.overrides
}

export async function setDayOverride(projectId: string, assignmentId: string, date: string, hours: number): Promise<AssignmentDayOverride> {
	const response = await fetch(projectUrl(projectId, `/assignments/${assignmentId}/day-overrides`), {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify({ date, hours }),
	})
	if (!response.ok) throw new Error('Failed to set day override')
	return AssignmentDayOverrideSchema.parse(await response.json())
}

export async function deleteDayOverride(projectId: string, assignmentId: string, date: string): Promise<void> {
	const response = await fetch(projectUrl(projectId, `/assignments/${assignmentId}/day-overrides/${date}`), {
		method: 'DELETE',
	})
	if (!response.ok) throw new Error('Failed to delete day override')
}

export async function deleteAssignment(projectId: string, id: string): Promise<void> {
	const response = await fetch(projectUrl(projectId, `/assignments/${id}`), {
		method: 'DELETE',
	})
	if (!response.ok) throw new Error('Failed to delete assignment')
}

export async function getResourceLoad(projectId: string, from: string, to: string): Promise<OverloadReport> {
	const response = await fetch(projectUrl(projectId, `/resource-load?from=${from}&to=${to}`), {
		headers: { Accept: 'application/json' },
	})
	if (!response.ok) throw new Error('Failed to fetch resource load')
	return OverloadReportSchema.parse(await response.json())
}

export async function previewLeveling(projectId: string): Promise<LevelingResult> {
	const response = await fetch(projectUrl(projectId, '/leveling/preview'), {
		method: 'POST',
		headers: { Accept: 'application/json' },
	})
	if (!response.ok) throw new Error('Failed to preview leveling')
	return LevelingResultSchema.parse(await response.json())
}

export async function applyLeveling(projectId: string): Promise<LevelingResult> {
	const response = await fetch(projectUrl(projectId, '/leveling/apply'), {
		method: 'POST',
		headers: { Accept: 'application/json' },
	})
	if (!response.ok) throw new Error('Failed to apply leveling')
	return LevelingResultSchema.parse(await response.json())
}

export async function getResourceLoadById(
	projectId: string,
	resourceId: string,
	from: string,
	to: string,
): Promise<ResourceLoadResult> {
	const response = await fetch(projectUrl(projectId, `/resource-load/${resourceId}?from=${from}&to=${to}`), {
		headers: { Accept: 'application/json' },
	})
	if (!response.ok) throw new Error('Failed to fetch resource load')
	return ResourceLoadResultSchema.parse(await response.json())
}
