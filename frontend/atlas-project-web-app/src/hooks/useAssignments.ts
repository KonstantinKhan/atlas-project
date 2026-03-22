import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
	getAssignments,
	createAssignment,
	updateAssignment,
	deleteAssignment,
	getResourceLoad,
	previewLeveling,
	applyLeveling,
	getDayOverrides,
	setDayOverride,
	deleteDayOverride,
} from '@/services/assignmentsApi'
import type { TaskAssignment, AssignmentDayOverride, OverloadReport, LevelingResult } from '@/types/schemas/assignment.schema'

export function useAssignments(projectId: string) {
	return useQuery<TaskAssignment[]>({
		queryKey: ['assignments', projectId],
		queryFn: () => getAssignments(projectId),
		enabled: !!projectId,
	})
}

export function useCreateAssignment(projectId: string) {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: ({ taskId, resourceId, hoursPerDay }: { taskId: string; resourceId: string; hoursPerDay?: number }) =>
			createAssignment(projectId, taskId, resourceId, hoursPerDay),
		onSuccess: () => {
			queryClient.invalidateQueries({ queryKey: ['assignments', projectId] })
			queryClient.invalidateQueries({ queryKey: ['resourceLoad', projectId] })
		},
	})
}

export function useUpdateAssignment(projectId: string) {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: ({ id, ...updates }: { id: string; hoursPerDay?: number; plannedEffortHours?: number | null }) =>
			updateAssignment(projectId, id, updates),
		onSuccess: () => {
			queryClient.invalidateQueries({ queryKey: ['assignments', projectId] })
			queryClient.invalidateQueries({ queryKey: ['resourceLoad', projectId] })
		},
	})
}

export function useDeleteAssignment(projectId: string) {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: ({ id }: { id: string }) => deleteAssignment(projectId, id),
		onSuccess: () => {
			queryClient.invalidateQueries({ queryKey: ['assignments', projectId] })
			queryClient.invalidateQueries({ queryKey: ['resourceLoad', projectId] })
		},
	})
}

export function useDayOverrides(projectId: string, assignmentId: string | null) {
	return useQuery<AssignmentDayOverride[]>({
		queryKey: ['dayOverrides', projectId, assignmentId],
		queryFn: () => getDayOverrides(projectId, assignmentId!),
		enabled: !!projectId && !!assignmentId,
	})
}

export function useSetDayOverride(projectId: string) {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: ({ assignmentId, date, hours }: { assignmentId: string; date: string; hours: number }) =>
			setDayOverride(projectId, assignmentId, date, hours),
		onSuccess: () => {
			queryClient.invalidateQueries({ queryKey: ['dayOverrides', projectId] })
			queryClient.invalidateQueries({ queryKey: ['resourceLoad', projectId] })
		},
	})
}

export function useDeleteDayOverride(projectId: string) {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: ({ assignmentId, date }: { assignmentId: string; date: string }) =>
			deleteDayOverride(projectId, assignmentId, date),
		onSuccess: () => {
			queryClient.invalidateQueries({ queryKey: ['dayOverrides', projectId] })
			queryClient.invalidateQueries({ queryKey: ['resourceLoad', projectId] })
		},
	})
}

export function useResourceLoad(projectId: string, from: string | null, to: string | null) {
	return useQuery<OverloadReport>({
		queryKey: ['resourceLoad', projectId, from, to],
		queryFn: () => getResourceLoad(projectId, from!, to!),
		enabled: !!projectId && !!from && !!to,
	})
}

export function useLevelingPreview(projectId: string) {
	return useMutation<LevelingResult>({
		mutationFn: () => previewLeveling(projectId),
	})
}

export function useApplyLeveling(projectId: string) {
	const queryClient = useQueryClient()
	return useMutation<LevelingResult>({
		mutationFn: () => applyLeveling(projectId),
		onSuccess: () => {
			queryClient.invalidateQueries({ queryKey: ['projectPlan', projectId] })
			queryClient.invalidateQueries({ queryKey: ['resourceLoad', projectId] })
			queryClient.invalidateQueries({ queryKey: ['criticalPath', projectId] })
		},
	})
}
