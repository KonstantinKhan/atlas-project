import { WorkCalendarSchema, WorkCalendar } from '@/types'

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://0.0.0.0:8080'

export async function getWorkCalendar(): Promise<WorkCalendar> {
	const response = await fetch(`${API_BASE_URL}/work-calendar`, {
		headers: {
			Accept: 'application/json',
		},
	})
	if (!response.ok) throw new Error('Failed to fetch work calendar')
	return WorkCalendarSchema.parse(await response.json())
}

export async function updateWorkCalendar(
	calendar: WorkCalendar,
): Promise<WorkCalendar> {
	const response = await fetch(`${API_BASE_URL}/work-calendar`, {
		method: 'PUT',
		headers: {
			'Content-Type': 'application/json',
			Accept: 'application/json',
		},
		body: JSON.stringify(calendar),
	})
	if (!response.ok) throw new Error('Failed to update work calendar')
	return WorkCalendarSchema.parse(await response.json())
}
