'use client'

import { useState, useRef, useCallback, useEffect } from 'react'
import { useTimelineCalendar } from '@/hooks/useTimelineCalendar'
import {
	useProjectPlan,
	useCreateProjectTask,
	useChangeTaskStartDate,
	useChangeTaskEndDate,
	useCreateDependency,
} from '@/hooks/useProjectTasks'
import { Task, GanttProjectPlan, TaskCommand } from '@/types'
import {
	getCalendarRange,
	getDaysInRange,
	groupDaysByMonth,
} from '@/utils/ganttDateUtils'
import GanttTaskList from './GanttTaskList'
import GanttCalendarHeader from './GanttCalendarHeader'
import GanttCalendarGrid from './GanttCalendarGrid'
import { TaskCommandType } from '@/types/types/TaskCommandType'

const DAY_COLUMN_WIDTH = 40
const ROW_HEIGHT = 48
const HEADER_HEIGHT = 60

function planToTasks(plan: GanttProjectPlan): Task[] {
	console.log('[GanttChart] Plan tasks:', plan.tasks.map(t => ({ id: t.id, title: t.title, start: t.start, end: t.end })))
	console.log('[GanttChart] Dependencies:', plan.dependencies)
	
	return plan.tasks.map((ganttTask) => {
		const deps = plan.dependencies.filter((d) => d.toTaskId === ganttTask.id)
		return {
			id: ganttTask.id,
			title: ganttTask.title,
			description: '',
			plannedCalendarDuration: undefined,
			actualCalendarDuration: undefined,
			plannedStartDate: ganttTask.start,
			plannedEndDate: ganttTask.end,
			actualStartDate: undefined,
			actualEndDate: undefined,
			status: ganttTask.status,
			dependsOn: deps.map((d) => d.fromTaskId),
			dependsOnLag: Object.fromEntries(deps.map((d) => [d.fromTaskId, d.lagDays])),
		}
	})
}

export const GanttChart = () => {
	const {
		calendar,
		isLoading: calendarLoading,
		error: calendarError,
		refetch: refetchCalendar,
	} = useTimelineCalendar()

	const {
		data: planData,
		isLoading: planLoading,
		error: planError,
		refetch: refetchPlan,
	} = useProjectPlan()

	const [tasks, setTasks] = useState<Task[]>([])
	const createTaskMutation = useCreateProjectTask()
	const changeStartMutation = useChangeTaskStartDate()
	const changeEndMutation = useChangeTaskEndDate()
	const createDependencyMutation = useCreateDependency()
	const leftRef = useRef<HTMLDivElement>(null)
	const rightRef = useRef<HTMLDivElement>(null)
	const isSyncing = useRef(false)

	useEffect(() => {
		if (planData) {
			setTasks(planToTasks(planData))
		}
	}, [planData])

	const handleAddTask = useCallback(() => {
		createTaskMutation.mutate(undefined, {
			onError: (error) => {
				console.error('[GanttChart] Failed to create task:', error)
			},
		})
	}, [createTaskMutation])

	const handleUpdateTask = useCallback(
		(cmd: TaskCommand) => {
			if (!planData) {
				throw new Error('handleUpdateTask called without planData')
			}

			switch (cmd.type) {
				case TaskCommandType.ChangeStartDate:
					changeStartMutation.mutate({
						planId: planData.projectId,
						taskId: cmd.taskId,
						newPlannedStart: cmd.newStartDate,
					})
					break
				case TaskCommandType.ChangeEndDate:
					changeEndMutation.mutate({
						planId: planData.projectId,
						taskId: cmd.taskId,
						newPlannedEnd: cmd.newEndDate,
					})
					break
				case TaskCommandType.UpdateTitle:
					setTasks((prev) =>
						prev.map((t) => (t.id === cmd.taskId ? { ...t, title: cmd.newTitle } : t)),
					)
					break
				default: {
					const _exhaustive: never = cmd
					throw new Error(`Unhandled command: ${JSON.stringify(_exhaustive)}`)
				}
			}
		},
		[changeStartMutation, changeEndMutation, planData],
	)

	const handleCreateDependency = useCallback((fromId: string, toId: string) => {
		if (fromId === toId) return
		if (!planData) return
		
		console.log('[GanttChart] Creating dependency:', { fromId, toId, planId: planData.projectId })
		
		// Backend рассчитает lag на основе текущих дат задач
		createDependencyMutation.mutate({
			planId: planData.projectId,
			fromTaskId: fromId,
			toTaskId: toId,
			type: 'FS',
		}, {
			onSuccess: (newPlan) => {
				console.log('[GanttChart] Dependency created, new plan:', newPlan.tasks.map(t => ({ id: t.id, start: t.start, end: t.end })))
				setTasks(planToTasks(newPlan))
			},
			onError: (error) => {
				console.error('[GanttChart] Error creating dependency:', error)
			}
		})
	}, [planData, createDependencyMutation])

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

	if (calendarLoading || planLoading) {
		return (
			<div className="flex h-screen items-center justify-center bg-white dark:bg-zinc-950">
				<div className="text-center">
					<div className="mb-4 h-8 w-8 animate-spin rounded-full border-4 border-blue-500 border-t-transparent"></div>
					<p className="text-gray-600 dark:text-zinc-400">Загрузка...</p>
				</div>
			</div>
		)
	}

	if (calendarError || planError) {
		const message = ((calendarError || planError) as Error).message
		return (
			<div className="flex h-screen items-center justify-center bg-white dark:bg-zinc-950">
				<div className="text-center">
					<p className="mb-4 text-red-600 dark:text-red-400">
						Ошибка загрузки данных: {message}
					</p>
					<button
						onClick={() => {
							refetchCalendar()
							refetchPlan()
						}}
						className="rounded bg-blue-500 px-4 py-2 text-white hover:bg-blue-600"
					>
						Повторить
					</button>
				</div>
			</div>
		)
	}

	if (!calendar || !planData) return null

	const { start: rangeStart, end: rangeEnd } = getCalendarRange(tasks)
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
