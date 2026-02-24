import { TaskListSchema, TaskSchema, Task } from '@/types'

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

export async function updateProjectTask(id: string, updates: object): Promise<Task> {
	const res = await fetch(`${API_BASE_URL}/project-tasks/${id}`, {
		method: 'PATCH',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify(updates),
	})
	if (!res.ok) throw new Error('Failed to update project task')
	return TaskSchema.parse(await res.json())
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
