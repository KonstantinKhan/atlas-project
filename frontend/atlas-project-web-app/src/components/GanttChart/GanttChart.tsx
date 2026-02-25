'use client'

import { useState, useRef, useCallback, useEffect } from 'react'
import { useTimelineCalendar } from '@/hooks/useTimelineCalendar'
import {
	useProjectTasks,
	useCreateProjectTask,
	useUpdateProjectTask,
} from '@/hooks/useProjectTasks'
import { Task } from '@/types'
import {
	formatDateForInput,
	getCalendarRange,
	getDaysInRange,
	groupDaysByMonth,
} from '@/utils/ganttDateUtils'
import { cascadeDependencies, daysBetween } from '@/utils/ganttDependencyUtils'
import GanttTaskList from './GanttTaskList'
import GanttCalendarHeader from './GanttCalendarHeader'
import GanttCalendarGrid from './GanttCalendarGrid'

const DAY_COLUMN_WIDTH = 40
const ROW_HEIGHT = 48
const HEADER_HEIGHT = 60

export const GanttChart = () => {
	const {
		calendar,
		isLoading: calendarLoading,
		error: calendarError,
		refetch: refetchCalendar,
	} = useTimelineCalendar()

	const {
		data: tasksData,
		isLoading: tasksLoading,
		error: tasksError,
		refetch: refetchTasks,
	} = useProjectTasks()

	const [tasks, setTasks] = useState<Task[]>(tasksData ?? [])
	const createTaskMutation = useCreateProjectTask()
	const updateTaskMutation = useUpdateProjectTask()
	const leftRef = useRef<HTMLDivElement>(null)
	const rightRef = useRef<HTMLDivElement>(null)
	const isSyncing = useRef(false)

	useEffect(() => {
		if (tasksData) {
			setTasks(tasksData)
		}
	}, [tasksData])

	const handleAddTask = useCallback(() => {
		createTaskMutation.mutate(undefined, {
			onSuccess: (newTask) => {
				setTasks((prev) => [...prev, newTask])
			},
		})
	}, [createTaskMutation])

	const handleUpdateTask = useCallback(
		(id: string, updates: Partial<Task>) => {
			if ('plannedStartDate' in updates) {
				updateTaskMutation.mutate(
					{
						id,
						updates: {
							plannedStartDate: formatDateForInput(updates.plannedStartDate!),
						},
					},
					{
						onSuccess: (updatedTask) => {
							setTasks((prev) => {
								const updated = prev.map((t) => (t.id === id ? updatedTask : t))
								return cascadeDependencies(updated, id)
							})
						},
					},
				)
			} else {
				setTasks((prev) => {
					const updated = prev.map((t) =>
						t.id === id ? { ...t, ...updates } : t,
					)
					return 'plannedEndDate' in updates
						? cascadeDependencies(updated, id)
						: updated
				})
			}
		},
		[updateTaskMutation],
	)

	const handleCreateDependency = useCallback((fromId: string, toId: string) => {
		if (fromId === toId) return
		setTasks((prev) => {
			const fromTask = prev.find((t) => t.id === fromId)
			const toTask = prev.find((t) => t.id === toId)
			if (!toTask || toTask.dependsOn?.includes(fromId)) return prev

			let lag = 0
			if (fromTask?.plannedEndDate && toTask.plannedStartDate) {
				lag = Math.max(
					0,
					daysBetween(fromTask.plannedEndDate, toTask.plannedStartDate) - 1,
				)
			}

			const updated = prev.map((t) =>
				t.id === toId
					? {
							...t,
							dependsOn: [...(t.dependsOn ?? []), fromId],
							dependsOnLag: { ...(t.dependsOnLag ?? {}), [fromId]: lag },
						}
					: t,
			)
			return cascadeDependencies(updated, fromId)
		})
	}, [])

	const handleRemoveDependency = useCallback((fromId: string, toId: string) => {
		setTasks((prev) =>
			prev.map((t) => {
				if (t.id !== toId) return t
				const newLag = { ...(t.dependsOnLag ?? {}) }
				delete newLag[fromId]
				return {
					...t,
					dependsOn: t.dependsOn?.filter((id) => id !== fromId),
					dependsOnLag: newLag,
				}
			}),
		)
	}, [])

	const syncScroll = useCallback(
		(source: 'left' | 'right') => () => {
			if (isSyncing.current) return
			isSyncing.current = true
			const from = source === 'left' ? leftRef.current : rightRef.current
			const to = source === 'left' ? rightRef.current : leftRef.current
			if (from && to) {
				to.scrollTop = from.scrollTop
			}
			requestAnimationFrame(() => {
				isSyncing.current = false
			})
		},
		[],
	)

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
						onClick={() => {
							refetchCalendar()
							refetchTasks()
						}}
						className="rounded bg-blue-500 px-4 py-2 text-white hover:bg-blue-600"
					>
						Повторить
					</button>
				</div>
			</div>
		)
	}

	if (!calendar || !tasksData) return null

	const { start: rangeStart, end: rangeEnd } = getCalendarRange(tasksData)
	const days = getDaysInRange(rangeStart, rangeEnd)
	const monthGroups = groupDaysByMonth(days)

	return (
		<div className="flex h-screen bg-white dark:bg-zinc-950 overflow-hidden">
			<div
				ref={leftRef}
				onScroll={syncScroll('left')}
				className="w-105 shrink-0 border-r border-gray-300 dark:border-zinc-700 overflow-y-auto"
			>
				<GanttTaskList
					tasks={tasks}
					headerHeight={HEADER_HEIGHT}
					rowHeight={ROW_HEIGHT}
					onAddTask={handleAddTask}
					onUpdateTask={handleUpdateTask}
				/>
			</div>

			<div
				ref={rightRef}
				onScroll={syncScroll('right')}
				className="flex-1 overflow-auto"
			>
				<div className="sticky top-0 z-10 bg-white dark:bg-zinc-950">
					<GanttCalendarHeader
						days={days}
						monthGroups={monthGroups}
						dayWidth={DAY_COLUMN_WIDTH}
						headerHeight={HEADER_HEIGHT}
						workCalendar={calendar}
					/>
				</div>
				<GanttCalendarGrid
					tasks={tasks}
					days={days}
					rangeStart={rangeStart}
					dayWidth={DAY_COLUMN_WIDTH}
					rowHeight={ROW_HEIGHT}
					timelineCalendar={calendar}
					onCreateDependency={handleCreateDependency}
					onRemoveDependency={handleRemoveDependency}
				/>
			</div>
		</div>
	)
}
