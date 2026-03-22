import {
	ResourceSchema,
	ResourceListSchema,
	ResourceCalendarOverrideListSchema,
	ResourceCalendarOverrideSchema,
	type Resource,
	type ResourceCalendarOverride,
} from '@/types/schemas/resource.schema'

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'

export async function getResources(): Promise<Resource[]> {
	const response = await fetch(`${API_BASE_URL}/resources`, {
		headers: { Accept: 'application/json' },
	})
	if (!response.ok) throw new Error('Failed to fetch resources')
	const data = ResourceListSchema.parse(await response.json())
	return data.resources
}

export async function createResource(name: string, type: string, capacityHoursPerDay: number): Promise<Resource> {
	const response = await fetch(`${API_BASE_URL}/resources`, {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify({ name, type, capacityHoursPerDay }),
	})
	if (!response.ok) throw new Error('Failed to create resource')
	return ResourceSchema.parse(await response.json())
}

export async function updateResource(
	id: string,
	updates: { name?: string; type?: string; capacityHoursPerDay?: number },
): Promise<Resource> {
	const response = await fetch(`${API_BASE_URL}/resources/${id}`, {
		method: 'PATCH',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify(updates),
	})
	if (!response.ok) throw new Error('Failed to update resource')
	return ResourceSchema.parse(await response.json())
}

export async function deleteResource(id: string): Promise<void> {
	const response = await fetch(`${API_BASE_URL}/resources/${id}`, {
		method: 'DELETE',
	})
	if (!response.ok) throw new Error('Failed to delete resource')
}

export async function getCalendarOverrides(resourceId: string): Promise<ResourceCalendarOverride[]> {
	const response = await fetch(`${API_BASE_URL}/resources/${resourceId}/calendar-overrides`, {
		headers: { Accept: 'application/json' },
	})
	if (!response.ok) throw new Error('Failed to fetch calendar overrides')
	const data = ResourceCalendarOverrideListSchema.parse(await response.json())
	return data.overrides
}

export async function setCalendarOverride(
	resourceId: string,
	date: string,
	availableHours: number,
): Promise<ResourceCalendarOverride> {
	const response = await fetch(`${API_BASE_URL}/resources/${resourceId}/calendar-overrides`, {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify({ date, availableHours }),
	})
	if (!response.ok) throw new Error('Failed to set calendar override')
	return ResourceCalendarOverrideSchema.parse(await response.json())
}

export async function deleteCalendarOverride(resourceId: string, date: string): Promise<void> {
	const response = await fetch(`${API_BASE_URL}/resources/${resourceId}/calendar-overrides/${date}`, {
		method: 'DELETE',
	})
	if (!response.ok) throw new Error('Failed to delete calendar override')
}
