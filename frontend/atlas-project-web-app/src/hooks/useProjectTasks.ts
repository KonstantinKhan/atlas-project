import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getProjectTasks, createProjectTask, updateProjectTask, getProjectPlan, changeTaskStartDate } from '@/services/projectTasksApi'
import { Task, GanttProjectPlan, ScheduleDelta } from '@/types'

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

export function useChangeTaskStartDate() {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: ({ planId, taskId, newPlannedStart }: { planId: string; taskId: string; newPlannedStart: string }) =>
			changeTaskStartDate(planId, taskId, newPlannedStart),
		onSuccess: (delta: ScheduleDelta) => {
			queryClient.setQueryData<GanttProjectPlan>(['projectPlan'], (old) => {
				if (!old) return old
				return {
					...old,
					tasks: old.tasks.map((t) => {
						const update = delta.updatedSchedules.find((u) => u.taskId === t.id)
						return update ? { ...t, start: update.start, end: update.end } : t
					}),
				}
			})
		},
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
