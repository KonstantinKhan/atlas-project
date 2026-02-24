import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getWorkCalendar, updateWorkCalendar } from '@/services/workCalendarApi'
import { useWorkCalendarStore } from '@/store/workCalendarStore'
import { WorkCalendar } from '@/types'
import { useEffect } from 'react'

const QUERY_KEY = ['workCalendar']

export function useWorkCalendar() {
	const queryClient = useQueryClient()
	const setCalendar = useWorkCalendarStore((state) => state.setCalendar)

	const {
		data: calendar,
		isLoading,
		error,
		refetch,
	} = useQuery<WorkCalendar>({
		queryKey: QUERY_KEY,
		queryFn: getWorkCalendar,
	})

	// Синхронизация с Zustand store при изменении данных
	useEffect(() => {
		if (calendar) {
			setCalendar(calendar)
		}
	}, [calendar, setCalendar])

	// Обновление календаря
	const updateMutation = useMutation({
		mutationFn: updateWorkCalendar,
		onSuccess: (data) => {
			// Инвалидируем и обновляем кэш
			queryClient.setQueryData(QUERY_KEY, data)
			setCalendar(data)
		},
	})

	// Обновление с optimistic update
	const updateCalendar = async (newCalendar: WorkCalendar) => {
		// Оптимистичное обновление кэша
		const previous = queryClient.getQueryData<WorkCalendar>(QUERY_KEY)
		queryClient.setQueryData(QUERY_KEY, newCalendar)
		setCalendar(newCalendar)

		try {
			await updateMutation.mutateAsync(newCalendar)
		} catch {
			// Откат при ошибке
			if (previous) {
				queryClient.setQueryData(QUERY_KEY, previous)
				setCalendar(previous)
			}
			throw new Error('Failed to update calendar')
		}
	}

	return {
		calendar: calendar as WorkCalendar | undefined,
		isLoading,
		error,
		refetch,
		updateCalendar,
		isUpdating: updateMutation.isPending,
	}
}
