import { useQuery } from '@tanstack/react-query'
import { getProjectTasks } from '@/services/projectTasksApi'
import { Task } from '@/types'

const QUERY_KEY = ['projectTasks']

export function useProjectTasks() {
	return useQuery<Task[]>({
		queryKey: QUERY_KEY,
		queryFn: getProjectTasks,
	})
}
