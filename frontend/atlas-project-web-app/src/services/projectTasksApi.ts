import { TaskListSchema, Task } from '@/types'

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
