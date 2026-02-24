'use client'

import { useState, useRef, useCallback, useEffect } from 'react'
import { Task, WorkCalendar } from '@/types'
import { useCreateProjectTask, useUpdateProjectTask } from '@/hooks/useProjectTasks'
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

interface GanttChartProps {
	initialTasks: Task[]
	workCalendar: WorkCalendar
}

export default function GanttChart({
	initialTasks,
	workCalendar,
}: GanttChartProps) {
	const [tasks, setTasks] = useState<Task[]>(initialTasks)

	useEffect(() => {
		setTasks(initialTasks)
	}, [initialTasks])

	const createTaskMutation = useCreateProjectTask()
	const updateTaskMutation = useUpdateProjectTask()
	const leftRef = useRef<HTMLDivElement>(null)
	const rightRef = useRef<HTMLDivElement>(null)
	const isSyncing = useRef(false)

	const { start: rangeStart, end: rangeEnd } = getCalendarRange(tasks)
	const days = getDaysInRange(rangeStart, rangeEnd)
	const monthGroups = groupDaysByMonth(days)

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
					{ id, updates: { plannedStartDate: formatDateForInput(updates.plannedStartDate!) } },
					{
						onSuccess: (updatedTask) => {
							setTasks((prev) => {
								const updated = prev.map((t) => t.id === id ? updatedTask : t)
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
						workCalendar={workCalendar}
					/>
				</div>
				<GanttCalendarGrid
					tasks={tasks}
					days={days}
					rangeStart={rangeStart}
					dayWidth={DAY_COLUMN_WIDTH}
					rowHeight={ROW_HEIGHT}
					workCalendar={workCalendar}
					onCreateDependency={handleCreateDependency}
					onRemoveDependency={handleRemoveDependency}
				/>
			</div>
		</div>
	)
}
