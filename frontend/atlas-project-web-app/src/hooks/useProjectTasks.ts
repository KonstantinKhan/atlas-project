import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
	getProjectTasks,
	createProjectTask,
	updateProjectTask,
	getProjectPlan,
	changeTaskStartDate,
	resizeTaskFromStart,
	changeTaskEndDate,
	createDependency,
	deleteProjectTask,
	deleteDependency,
	changeDependencyType,
	assignTaskSchedule,
	planTaskFromEnd,
	getCriticalPath,
	getBlockerChain,
	getAvailableTasks,
	getWhatIf,
	getWhatIfEnd,
	reorderTasks,
	saveBaseline,
} from '@/services/projectTasksApi'
import { Task, GanttProjectPlan, ScheduleDelta, CriticalPath, BlockerChain, AvailableTasks, WhatIfResult } from '@/types'

export function useProjectTasks(projectId: string) {
	return useQuery<Task[]>({
		queryKey: ['projectTasks', projectId],
		queryFn: () => getProjectTasks(projectId),
		enabled: !!projectId,
	})
}

export function useUpdateProjectTask(projectId: string) {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: ({ id, updates }: { id: string; updates: object }) =>
			updateProjectTask(projectId, id, updates),
		onSuccess: (updatedTask: Task) => {
			queryClient.invalidateQueries({ queryKey: ['projectTasks', projectId] })
			queryClient.invalidateQueries({ queryKey: ['criticalPath', projectId] })
			queryClient.setQueryData<GanttProjectPlan>(['projectPlan', projectId], (old) => {
				if (!old) return old
				return {
					...old,
					tasks: old.tasks.map((t) =>
						t.id === updatedTask.id
							? { ...t, title: updatedTask.title, description: updatedTask.description, status: updatedTask.status }
							: t,
					),
				}
			})
		},
	})
}

export function useProjectPlan(projectId: string) {
	return useQuery<GanttProjectPlan>({
		queryKey: ['projectPlan', projectId],
		queryFn: () => getProjectPlan(projectId),
		enabled: !!projectId,
	})
}

export function useCriticalPath(projectId: string) {
	return useQuery<CriticalPath>({
		queryKey: ['criticalPath', projectId],
		queryFn: () => getCriticalPath(projectId),
		staleTime: 5000,
		enabled: !!projectId,
	})
}

export function useChangeTaskStartDate(projectId: string) {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: ({
			planId,
			taskId,
			newPlannedStart,
		}: {
			planId: string
			taskId: string
			newPlannedStart: string
		}) => changeTaskStartDate(projectId, planId, taskId, newPlannedStart),
		onSuccess: (delta: ScheduleDelta) => {
			queryClient.invalidateQueries({ queryKey: ['criticalPath', projectId] })
			queryClient.setQueryData<GanttProjectPlan>(['projectPlan', projectId], (old) => {
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

export function useResizeTaskFromStart(projectId: string) {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: ({
			planId,
			taskId,
			newPlannedStart,
		}: {
			planId: string
			taskId: string
			newPlannedStart: string
		}) => resizeTaskFromStart(projectId, planId, taskId, newPlannedStart),
		onSuccess: (delta: ScheduleDelta) => {
			queryClient.invalidateQueries({ queryKey: ['criticalPath', projectId] })
			queryClient.setQueryData<GanttProjectPlan>(['projectPlan', projectId], (old) => {
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

export function useChangeTaskEndDate(projectId: string) {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: ({
			planId,
			taskId,
			newPlannedEnd,
		}: {
			planId: string
			taskId: string
			newPlannedEnd: string
		}) => changeTaskEndDate(projectId, planId, taskId, newPlannedEnd),
		onSuccess: (delta: ScheduleDelta) => {
			queryClient.invalidateQueries({ queryKey: ['criticalPath', projectId] })
			queryClient.setQueryData<GanttProjectPlan>(['projectPlan', projectId], (old) => {
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

export function useCreateProjectTask(projectId: string) {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: ({ title }: { title: string }) => createProjectTask(projectId, title),
		onSuccess: () => {
			queryClient.invalidateQueries({ queryKey: ['projectTasks', projectId] })
			queryClient.invalidateQueries({ queryKey: ['projectPlan', projectId] })
			queryClient.invalidateQueries({ queryKey: ['criticalPath', projectId] })
		},
	})
}

export function useDeleteProjectTask(projectId: string) {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: ({ id }: { id: string }) => deleteProjectTask(projectId, id),
		onSuccess: (_, { id }) => {
			queryClient.invalidateQueries({ queryKey: ['criticalPath', projectId] })
			queryClient.setQueryData<GanttProjectPlan>(['projectPlan', projectId], (old) => {
				if (!old) return old
				return {
					...old,
					tasks: old.tasks.filter((t) => t.id !== id),
					dependencies: old.dependencies.filter(
						(d) => d.fromTaskId !== id && d.toTaskId !== id
					),
				}
			})
		},
	})
}

export function useAssignTaskSchedule(projectId: string) {
	const queryClient = useQueryClient()
	return useMutation<GanttProjectPlan, Error, { taskId: string; start: string; duration: number }>({
		mutationFn: ({ taskId, start, duration }) => assignTaskSchedule(projectId, taskId, start, duration),
		onSuccess: (newPlan: GanttProjectPlan) => {
			queryClient.invalidateQueries({ queryKey: ['criticalPath', projectId] })
			queryClient.setQueryData<GanttProjectPlan>(['projectPlan', projectId], () => newPlan)
		},
	})
}

export function usePlanFromEnd(projectId: string) {
	const queryClient = useQueryClient()
	return useMutation<GanttProjectPlan, Error, { taskId: string; newPlannedEnd: string }>({
		mutationFn: ({ taskId, newPlannedEnd }) => planTaskFromEnd(projectId, taskId, newPlannedEnd),
		onSuccess: (newPlan: GanttProjectPlan) => {
			queryClient.invalidateQueries({ queryKey: ['criticalPath', projectId] })
			queryClient.setQueryData<GanttProjectPlan>(['projectPlan', projectId], () => newPlan)
		},
	})
}

export function useDeleteDependency(projectId: string) {
	const queryClient = useQueryClient()
	return useMutation<GanttProjectPlan, Error, { fromTaskId: string; toTaskId: string }>({
		mutationFn: ({ fromTaskId, toTaskId }) => deleteDependency(projectId, fromTaskId, toTaskId),
		onSuccess: (newPlan: GanttProjectPlan) => {
			queryClient.invalidateQueries({ queryKey: ['criticalPath', projectId] })
			queryClient.setQueryData<GanttProjectPlan>(['projectPlan', projectId], () => newPlan)
		},
	})
}

export function useChangeDependencyType(projectId: string) {
	const queryClient = useQueryClient()
	return useMutation<GanttProjectPlan, Error, {
		fromTaskId: string
		toTaskId: string
		newType: string
	}>({
		mutationFn: ({ fromTaskId, toTaskId, newType }) =>
			changeDependencyType(projectId, fromTaskId, toTaskId, newType),
		onSuccess: (newPlan: GanttProjectPlan) => {
			queryClient.invalidateQueries({ queryKey: ['criticalPath', projectId] })
			queryClient.setQueryData<GanttProjectPlan>(['projectPlan', projectId], () => newPlan)
		},
	})
}

export function useCreateDependency(projectId: string) {
	const queryClient = useQueryClient()
	return useMutation<GanttProjectPlan, Error, {
		planId: string
		fromTaskId: string
		toTaskId: string
		type?: string
	}>({
		mutationFn: ({ planId, fromTaskId, toTaskId, type = 'FS' }) =>
			createDependency(projectId, planId, fromTaskId, toTaskId, type),
		onSuccess: (newPlan: GanttProjectPlan) => {
			queryClient.invalidateQueries({ queryKey: ['criticalPath', projectId] })
			queryClient.setQueryData<GanttProjectPlan>(['projectPlan', projectId], () => newPlan)
		},
	})
}

export function useBlockerChain(projectId: string, taskId: string | null) {
	return useQuery<BlockerChain>({
		queryKey: ['blockerChain', projectId, taskId],
		queryFn: () => getBlockerChain(projectId, taskId!),
		enabled: !!projectId && !!taskId,
	})
}

export function useAvailableTasks(projectId: string) {
	const today = new Date().toISOString().slice(0, 10)
	return useQuery<AvailableTasks>({
		queryKey: ['availableTasks', projectId, today],
		queryFn: () => getAvailableTasks(projectId, today),
		staleTime: 60_000,
		enabled: !!projectId,
	})
}

export function useWhatIf(projectId: string, taskId: string | null, newStart: string | null) {
	return useQuery<WhatIfResult>({
		queryKey: ['whatIf', projectId, taskId, newStart],
		queryFn: () => getWhatIf(projectId, taskId!, newStart!),
		enabled: !!projectId && !!taskId && !!newStart,
	})
}

export function useWhatIfEnd(projectId: string, taskId: string | null, newEnd: string | null) {
	return useQuery<WhatIfResult>({
		queryKey: ['whatIf', 'end', projectId, taskId, newEnd],
		queryFn: () => getWhatIfEnd(projectId, taskId!, newEnd!),
		enabled: !!projectId && !!taskId && !!newEnd,
	})
}

export function useReorderTasks(projectId: string) {
	const queryClient = useQueryClient()
	return useMutation<GanttProjectPlan, Error, { orderedIds: string[] }>({
		mutationFn: ({ orderedIds }) => reorderTasks(projectId, orderedIds),
		onSuccess: (newPlan: GanttProjectPlan) => {
			queryClient.setQueryData<GanttProjectPlan>(['projectPlan', projectId], () => newPlan)
		},
	})
}

export function useSaveBaseline(projectId: string) {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: (taskIds?: string[]) => saveBaseline(projectId, taskIds),
		onSuccess: () => {
			queryClient.invalidateQueries({ queryKey: ['projectPlan', projectId] })
		},
	})
}
