import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getTimelineCalendar, updateTimelineCalendar } from '@/services/timelineCalendarApi'
import { TimelineCalendar } from '@/types'

const QUERY_KEY = ['timelineCalendar']

export function useTimelineCalendar() {
	const queryClient = useQueryClient()

	const {
		data: calendar,
		isLoading,
		error,
		refetch,
	} = useQuery<TimelineCalendar>({
		queryKey: QUERY_KEY,
		queryFn: getTimelineCalendar,
	})

	const updateMutation = useMutation({
		mutationFn: updateTimelineCalendar,
		onSuccess: (data) => {
			queryClient.setQueryData(QUERY_KEY, data)
		},
	})

	const updateCalendar = async (newCalendar: TimelineCalendar) => {
		const previous = queryClient.getQueryData<TimelineCalendar>(QUERY_KEY)
		queryClient.setQueryData(QUERY_KEY, newCalendar)

		try {
			await updateMutation.mutateAsync(newCalendar)
		} catch {
			if (previous) {
				queryClient.setQueryData(QUERY_KEY, previous)
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
