'use client'

import { GanttChart } from '@/components/GanttChart'
import { useWorkCalendar } from '@/hooks/useWorkCalendar'
import { useProjectTasks } from '@/hooks/useProjectTasks'

export default function Home() {
	const { calendar, isLoading: calendarLoading, error: calendarError, refetch: refetchCalendar } = useWorkCalendar()
	const { data: tasks, isLoading: tasksLoading, error: tasksError, refetch: refetchTasks } = useProjectTasks()

	if (calendarLoading || tasksLoading) {
		return (
			<div className="flex h-screen items-center justify-center bg-white dark:bg-zinc-950">
				<div className="text-center">
					<div className="mb-4 h-8 w-8 animate-spin rounded-full border-4 border-blue-500 border-t-transparent"></div>
					<p className="text-gray-600 dark:text-zinc-400">Загрузка...</p>
				</div>
			</div>
		)
	}

	if (calendarError || tasksError) {
		const message = ((calendarError || tasksError) as Error).message
		return (
			<div className="flex h-screen items-center justify-center bg-white dark:bg-zinc-950">
				<div className="text-center">
					<p className="mb-4 text-red-600 dark:text-red-400">
						Ошибка загрузки данных: {message}
					</p>
					<button
						onClick={() => { refetchCalendar(); refetchTasks() }}
						className="rounded bg-blue-500 px-4 py-2 text-white hover:bg-blue-600"
					>
						Повторить
					</button>
				</div>
			</div>
		)
	}

	if (!calendar || !tasks) return null

	return <GanttChart initialTasks={tasks} workCalendar={calendar} />
}
