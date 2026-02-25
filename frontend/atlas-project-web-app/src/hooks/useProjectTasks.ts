import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getProjectTasks, createProjectTask, updateProjectTask, getProjectPlan } from '@/services/projectTasksApi'
import { Task, GanttProjectPlan } from '@/types'

const QUERY_KEY = ['projectTasks']

export function useProjectTasks() {
	return useQuery<Task[]>({
		queryKey: QUERY_KEY,
		queryFn: getProjectTasks,
	})
}

export function useUpdateProjectTask() {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: ({ id, updates }: { id: string; updates: object }) =>
			updateProjectTask(id, updates),
		onSuccess: () =>
			queryClient.invalidateQueries({ queryKey: QUERY_KEY }),
	})
}

export function useProjectPlan() {
	return useQuery<GanttProjectPlan>({
		queryKey: ['projectPlan'],
		queryFn: getProjectPlan,
	})
}

export function useCreateProjectTask() {
	const queryClient = useQueryClient()
	return useMutation<Task>({
		mutationFn: createProjectTask,
		onSuccess: () => {
			queryClient.invalidateQueries({ queryKey: QUERY_KEY })
		},
	})
}
