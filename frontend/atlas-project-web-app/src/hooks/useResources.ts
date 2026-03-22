import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
	getResources,
	createResource,
	updateResource,
	deleteResource,
	getCalendarOverrides,
	setCalendarOverride,
	deleteCalendarOverride,
} from '@/services/resourcesApi'
import type { Resource, ResourceCalendarOverride } from '@/types/schemas/resource.schema'

export function useResources() {
	return useQuery<Resource[]>({
		queryKey: ['resources'],
		queryFn: getResources,
	})
}

export function useCreateResource() {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: ({ name, type, capacityHoursPerDay }: { name: string; type: string; capacityHoursPerDay: number }) =>
			createResource(name, type, capacityHoursPerDay),
		onSuccess: () => {
			queryClient.invalidateQueries({ queryKey: ['resources'] })
		},
	})
}

export function useUpdateResource() {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: ({
			id,
			updates,
		}: {
			id: string
			updates: { name?: string; type?: string; capacityHoursPerDay?: number }
		}) => updateResource(id, updates),
		onSuccess: () => {
			queryClient.invalidateQueries({ queryKey: ['resources'] })
		},
	})
}

export function useDeleteResource() {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: ({ id }: { id: string }) => deleteResource(id),
		onSuccess: () => {
			queryClient.invalidateQueries({ queryKey: ['resources'] })
		},
	})
}

export function useCalendarOverrides(resourceId: string | null) {
	return useQuery<ResourceCalendarOverride[]>({
		queryKey: ['calendarOverrides', resourceId],
		queryFn: () => getCalendarOverrides(resourceId!),
		enabled: !!resourceId,
	})
}

export function useSetCalendarOverride() {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: ({
			resourceId,
			date,
			availableHours,
		}: {
			resourceId: string
			date: string
			availableHours: number
		}) => setCalendarOverride(resourceId, date, availableHours),
		onSuccess: (_data, variables) => {
			queryClient.invalidateQueries({ queryKey: ['calendarOverrides', variables.resourceId] })
		},
	})
}

export function useDeleteCalendarOverride() {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: ({ resourceId, date }: { resourceId: string; date: string }) =>
			deleteCalendarOverride(resourceId, date),
		onSuccess: (_data, variables) => {
			queryClient.invalidateQueries({ queryKey: ['calendarOverrides', variables.resourceId] })
		},
	})
}
