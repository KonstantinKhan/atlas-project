import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getTimelineCalendar, updateTimelineCalendar } from '@/services/timelineCalendarApi'
import { useTimelineCalendarStore } from '@/store/timelineCalendarStore'
import { TimelineCalendar } from '@/types'
import { useEffect } from 'react'

const QUERY_KEY = ['timelineCalendar']

export function useTimelineCalendar() {
	const queryClient = useQueryClient()
	const setCalendar = useTimelineCalendarStore((state) => state.setCalendar)

	const {
		data: calendar,
		isLoading,
		error,
		refetch,
	} = useQuery<TimelineCalendar>({
		queryKey: QUERY_KEY,
		queryFn: getTimelineCalendar,
	})

	useEffect(() => {
		if (calendar) {
			setCalendar(calendar)
		}
	}, [calendar, setCalendar])

	const updateMutation = useMutation({
		mutationFn: updateTimelineCalendar,
		onSuccess: (data) => {
			queryClient.setQueryData(QUERY_KEY, data)
			setCalendar(data)
		},
	})

	const updateCalendar = async (newCalendar: TimelineCalendar) => {
		const previous = queryClient.getQueryData<TimelineCalendar>(QUERY_KEY)
		queryClient.setQueryData(QUERY_KEY, newCalendar)
		setCalendar(newCalendar)

		try {
			await updateMutation.mutateAsync(newCalendar)
		} catch {
			if (previous) {
				queryClient.setQueryData(QUERY_KEY, previous)
				setCalendar(previous)
			}
			throw new Error('Failed to update calendar')
		}
	}

	return {
		calendar,
		isLoading,
		error,
		refetch,
		updateCalendar,
		isUpdating: updateMutation.isPending,
	}
}
