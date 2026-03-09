'use client'

import { useState, useRef, useCallback, useEffect, useMemo } from 'react'
import { useTimelineCalendar } from '@/hooks/useTimelineCalendar'
import {
	useProjectPlan,
	useCriticalPath,
	useCreateProjectTask,
	useChangeTaskStartDate,
	useResizeTaskFromStart,
	useChangeTaskEndDate,
	useCreateDependency,
	useChangeDependencyType,
	useDeleteDependency,
	useUpdateProjectTask,
	useDeleteProjectTask,
	useAssignTaskSchedule,
	usePlanFromEnd,
} from '@/hooks/useProjectTasks'
import { GanttTask, GanttDependencyDto, TaskCommand } from '@/types'
import { ProjectTaskStatus } from '@/types/generated/enums/project-task-status.enum'
import {
	getCalendarRange,
	getDaysInRange,
	groupDaysByMonth,
	formatDateForInput,
	alignToMonday,
	getWeeksInRange,
	groupWeeksByMonth,
} from '@/utils/ganttDateUtils'
import { useTimelineCalendarStore, type ViewMode } from '@/store/timelineCalendarStore'
import GanttTaskList from './GanttTaskList'
import GanttCalendarHeader from './GanttCalendarHeader'
import GanttCalendarGrid, { TIMELINE_DROP_ID } from './GanttCalendarGrid'
import ConfirmDeleteModal from './ConfirmDeleteModal'
import Toast from './Toast'
import { TaskCommandType } from '@/types/types/TaskCommandType'
import { TaskId } from '@/utils/types/TaskId'
import { LocalDate } from '@/utils/types/LocalDate'
import { DragDropProvider } from '@dnd-kit/react'
import type { DragEndEvent } from '@dnd-kit/react'

type DragEndArg = Parameters<DragEndEvent>[0]

const DAY_WIDTH_DAILY = 40
const DAY_WIDTH_WEEKLY = 8
const WEEK_COLUMN_WIDTH = DAY_WIDTH_WEEKLY * 7
const ROW_HEIGHT = 48
const HEADER_HEIGHT = 60

function addDays(date: Date, days: number): Date {
	const result = new Date(date)
	result.setDate(result.getDate() + days)
	return result
}

export const GanttChart = () => {
	const viewMode = useTimelineCalendarStore((s) => s.ui.viewMode)
	const setViewMode = useTimelineCalendarStore((s) => s.setViewMode)
	const dayWidth = viewMode === 'day' ? DAY_WIDTH_DAILY : DAY_WIDTH_WEEKLY

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

	const [allTasks, setAllTasks] = useState<GanttTask[]>([])
	const [dependencies, setDependencies] = useState<GanttDependencyDto[]>([])
	const prevTasksRef = useRef<GanttTask[]>([])

	const createTaskMutation = useCreateProjectTask()
	const changeStartMutation = useChangeTaskStartDate()
	const resizeFromStartMutation = useResizeTaskFromStart()
	const changeEndMutation = useChangeTaskEndDate()
	const createDependencyMutation = useCreateDependency()
	const changeDependencyTypeMutation = useChangeDependencyType()
	const deleteDependencyMutation = useDeleteDependency()
	const updateTitleMutation = useUpdateProjectTask()
	const deleteTaskMutation = useDeleteProjectTask()
	const assignScheduleMutation = useAssignTaskSchedule()
	const planFromEndMutation = usePlanFromEnd()
	const { data: criticalPathData } = useCriticalPath()

	const criticalTaskIds = useMemo(() => {
		if (!criticalPathData) return undefined
		return new Set(criticalPathData.criticalTaskIds)
	}, [criticalPathData])

	const slackMap = useMemo(() => {
		if (!criticalPathData) return undefined
		const map = new Map<string, number>()
		for (const task of criticalPathData.tasks) {
			map.set(task.taskId, task.slack)
		}
		return map
	}, [criticalPathData])

	const [pendingDeleteTaskId, setPendingDeleteTaskId] = useState<string | null>(
		null,
	)
	const [toastMessage, setToastMessage] = useState<string | null>(null)
	const leftRef = useRef<HTMLDivElement>(null)
	const rightRef = useRef<HTMLDivElement>(null)
	const isSyncing = useRef(false)

	useEffect(() => {
		if (planData) {
			setAllTasks((prev) => {
				if (prev.length === 0) return planData.tasks
				const updatedById = new Map(planData.tasks.map((t) => [t.id, t]))
				const merged = prev
					.map((t) => updatedById.get(t.id) ?? t)
					.filter((t) => updatedById.has(t.id))
				const existingIds = new Set(prev.map((t) => t.id))
				const added = planData.tasks.filter((t) => !existingIds.has(t.id))
				return [...merged, ...added]
			})
			setDependencies(planData.dependencies)
		}
	}, [planData])

	// Helper: синхронизация состояния после успешной мутации
	const syncPlanState = useCallback(
		(newPlan: { tasks: GanttTask[]; dependencies: GanttDependencyDto[] }) => {
			setAllTasks((prev) =>
				prev.map((t) => {
					const updated = newPlan.tasks.find((nt) => nt.id === t.id)
					return updated ?? t
				}),
			)
			setDependencies(newPlan.dependencies)
		},
		[],
	)

	// Helper: единый паттерн для оптимистических обновлений полей
	const handleFieldUpdate = useCallback(
		(
			taskId: string,
			updates: Partial<{
				title: string
				description: string
				status: ProjectTaskStatus
			}>,
			errorMessage: string,
		) => {
			setAllTasks((prev) =>
				prev.map((t) => (t.id === taskId ? { ...t, ...updates } : t)),
			)
			updateTitleMutation.mutate(
				{ id: taskId, updates },
				{ onError: () => setToastMessage(errorMessage) },
			)
		},
		[updateTitleMutation],
	)

	// Helper: оптимистическое обновление с откатом при ошибке
	const optimisticMutation = useCallback(
		(
			optimisticUpdate: (prev: GanttTask[]) => GanttTask[],
			mutationFn: () => void,
		) => {
			setAllTasks((prev) => {
				prevTasksRef.current = prev
				return optimisticUpdate(prev)
			})
			mutationFn()
		},
		[],
	)

	const handleUpdateTask = useCallback(
		(cmd: TaskCommand) => {
			if (!planData) {
				throw new Error('handleUpdateTask called without planData')
			}

			// Обработчики для каждого типа команд
			switch (cmd.type) {
				case TaskCommandType.CreateTaskInPool:
					createTaskMutation.mutate(
						{ title: cmd.title },
						{
							onError: () => setToastMessage('Не удалось создать задачу'),
						},
					)
					break

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
					handleFieldUpdate(
						cmd.taskId,
						{ title: cmd.newTitle },
						'Не удалось обновить название задачи',
					)
					break

				case TaskCommandType.UpdateDescription:
					handleFieldUpdate(
						cmd.taskId,
						{ description: cmd.newDescription },
						'Не удалось обновить описание задачи',
					)
					break

				case TaskCommandType.ChangeStatus:
					handleFieldUpdate(
						cmd.taskId,
						{ status: cmd.newStatus },
						'Не удалось обновить статус задачи',
					)
					break

				case TaskCommandType.DeleteTask:
					setPendingDeleteTaskId(cmd.taskId)
					break

				case TaskCommandType.PlanFromEnd:
					planFromEndMutation.mutate(
						{ taskId: cmd.taskId, newPlannedEnd: cmd.newEndDate },
						{
							onSuccess: syncPlanState,
							onError: () =>
								setToastMessage('Не удалось запланировать задачу от конца'),
						},
					)
					break

				case TaskCommandType.AssignSchedule: {
					const optimisticUpdate = (prev: GanttTask[]) =>
						prev.map((t) =>
							t.id === cmd.taskId
								? {
										...t,
										start: new Date(cmd.start),
										end: addDays(new Date(cmd.start), cmd.duration),
									}
								: t,
						)
					optimisticMutation(optimisticUpdate, () =>
						assignScheduleMutation.mutate(
							{ taskId: cmd.taskId, start: cmd.start, duration: cmd.duration },
							{
								onSuccess: syncPlanState,
								onError: () =>
									setToastMessage('Не удалось назначить расписание'),
							},
						),
					)
					break
				}

				case TaskCommandType.MoveTask: {
					const optimisticUpdate = (prev: GanttTask[]) =>
						prev.map((t) => {
							if (t.id !== cmd.taskId || !t.start || !t.end) return t
							const diffMs =
								new Date(cmd.newStartDate).getTime() - t.start.getTime()
							return {
								...t,
								start: new Date(cmd.newStartDate),
								end: new Date(t.end.getTime() + diffMs),
							}
						})
					optimisticMutation(optimisticUpdate, () =>
						changeStartMutation.mutate(
							{
								planId: planData.projectId,
								taskId: cmd.taskId,
								newPlannedStart: cmd.newStartDate,
							},
							{
								onError: () => setToastMessage('Не удалось переместить задачу'),
							},
						),
					)
					break
				}

				case TaskCommandType.ResizeTask: {
					const optimisticUpdate = (prev: GanttTask[]) =>
						prev.map((t) =>
							t.id === cmd.taskId ? { ...t, end: new Date(cmd.newEndDate) } : t,
						)
					optimisticMutation(optimisticUpdate, () =>
						changeEndMutation.mutate(
							{
								planId: planData.projectId,
								taskId: cmd.taskId,
								newPlannedEnd: cmd.newEndDate,
							},
							{
								onError: () =>
									setToastMessage('Не удалось изменить длительность задачи'),
							},
						),
					)
					break
				}

				default: {
					const _exhaustive: never = cmd
					console.error(
						`[GanttChart] Unknown command type: ${(_exhaustive as any).type}`,
					)
				}
			}
		},
		[
			planData,
			createTaskMutation,
			changeStartMutation,
			changeEndMutation,
			updateTitleMutation,
			planFromEndMutation,
			assignScheduleMutation,
			handleFieldUpdate,
			optimisticMutation,
			syncPlanState,
		],
	)

	const handleCreateDependency = useCallback(
		(fromId: string, toId: string, type: string) => {
			if (fromId === toId) return
			if (!planData) return

			createDependencyMutation.mutate(
				{
					planId: planData.projectId,
					fromTaskId: fromId,
					toTaskId: toId,
					type,
				},
				{
					onSuccess: (newPlan) => {
						setAllTasks((prev) =>
							prev.map((t) => {
								const updated = newPlan.tasks.find((nt) => nt.id === t.id)
								return updated ?? t
							}),
						)
						setDependencies(newPlan.dependencies)
					},
					onError: (error) => {
						console.error('[GanttChart] Error creating dependency:', error)
					},
				},
			)
		},
		[planData, createDependencyMutation],
	)

	const handleChangeDependencyType = useCallback(
		(fromId: string, toId: string, newType: string) => {
			setDependencies((prev) =>
				prev.map((d) =>
					d.fromTaskId === fromId && d.toTaskId === toId
						? { ...d, type: newType }
						: d,
				),
			)
			changeDependencyTypeMutation.mutate(
				{ fromTaskId: fromId, toTaskId: toId, newType },
				{
					onSuccess: (newPlan) => {
						setAllTasks((prev) =>
							prev.map((t) => {
								const updated = newPlan.tasks.find((nt) => nt.id === t.id)
								return updated ?? t
							}),
						)
						setDependencies(newPlan.dependencies)
					},
					onError: () => {
						refetchPlan()
					},
				},
			)
		},
		[changeDependencyTypeMutation, refetchPlan],
	)

	const handleRemoveDependency = useCallback(
		(fromId: string, toId: string) => {
			setDependencies((prev) =>
				prev.filter((d) => !(d.fromTaskId === fromId && d.toTaskId === toId)),
			)
			deleteDependencyMutation.mutate(
				{ fromTaskId: fromId, toTaskId: toId },
				{
					onSuccess: (newPlan) => {
						setAllTasks((prev) =>
							prev.map((t) => {
								const updated = newPlan.tasks.find((nt) => nt.id === t.id)
								return updated ?? t
							}),
						)
						setDependencies(newPlan.dependencies)
					},
					onError: () => {
						refetchPlan()
					},
				},
			)
		},
		[deleteDependencyMutation, refetchPlan],
	)

	const handleMoveTask = useCallback(
		(taskId: string, newStartDate: string) => {
			if (!planData) return
			handleUpdateTask({
				type: TaskCommandType.MoveTask,
				taskId: TaskId(taskId),
				newStartDate: LocalDate(newStartDate),
			})
		},
		[handleUpdateTask, planData],
	)

	const handleResizeTask = useCallback(
		(taskId: string, newEndDate: string) => {
			if (!planData) return
			handleUpdateTask({
				type: TaskCommandType.ResizeTask,
				taskId: TaskId(taskId),
				newEndDate: LocalDate(newEndDate),
			})
		},
		[handleUpdateTask, planData],
	)

	const handleResizeFromStart = useCallback(
		(taskId: string, newStartDate: string) => {
			if (!planData) return
			setAllTasks((prev) => {
				prevTasksRef.current = prev
				return prev.map((t) => {
					if (t.id !== taskId || !t.end) return t
					return { ...t, start: new Date(newStartDate) }
				})
			})
			resizeFromStartMutation.mutate(
				{ planId: planData.projectId, taskId, newPlannedStart: newStartDate },
				{
					onSuccess: (delta) => {
						setAllTasks((prev) =>
							prev.map((t) => {
								const update = delta.updatedSchedules.find(
									(u) => u.taskId === t.id,
								)
								return update
									? { ...t, start: update.start, end: update.end }
									: t
							}),
						)
					},
					onError: () => {
						setAllTasks(prevTasksRef.current)
						setToastMessage('Не удалось изменить начало задачи')
					},
				},
			)
		},
		[planData, resizeFromStartMutation],
	)

	const handleDragEnd = useCallback(
		(event: DragEndArg) => {
			if (event.canceled) return
			const { operation } = event
			if (!operation.source || !operation.target) return
			if (operation.target.id !== TIMELINE_DROP_ID) return

			const taskId = String(operation.source.id)
			const task = allTasks.find((t) => t.id === taskId)
			if (!task || task.start || task.end) return // only pool tasks

			const nativeEvent = event.nativeEvent as PointerEvent | undefined
			const droppableElement = operation.target.element
			if (!nativeEvent || !droppableElement) return

			const rect = droppableElement.getBoundingClientRect()
			const dropX = nativeEvent.clientX - rect.left
			const snapDays = viewMode === 'week' ? 7 : 1
		const dayOffset = Math.max(0, Math.round(dropX / (dayWidth * snapDays)) * snapDays)

			if (!planData) return
			const { start: rangeStart } = getCalendarRange(
				allTasks.filter((t) => t.start && t.end),
			)
			const dropDate = addDays(rangeStart, dayOffset)

			handleUpdateTask({
				type: TaskCommandType.AssignSchedule,
				taskId: TaskId(taskId),
				start: LocalDate(formatDateForInput(dropDate)),
				duration: 1,
			})
		},
		[allTasks, planData, handleUpdateTask],
	)

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

	const tasksWithDates = allTasks.filter((t) => t.start && t.end)
	const { start: rawRangeStart, end: rangeEnd } = getCalendarRange(tasksWithDates)
	const rangeStart = viewMode === 'week' ? alignToMonday(rawRangeStart) : rawRangeStart
	const days = getDaysInRange(rangeStart, rangeEnd)
	const weeks = viewMode === 'week' ? getWeeksInRange(rangeStart, rangeEnd) : []
	const monthGroups = viewMode === 'day' ? groupDaysByMonth(days) : groupWeeksByMonth(weeks)

	const pendingDeleteTask = pendingDeleteTaskId
		? allTasks.find((t) => t.id === pendingDeleteTaskId)
		: null
	const affectedDepsCount = pendingDeleteTaskId
		? dependencies.filter(
				(d) =>
					d.fromTaskId === pendingDeleteTaskId ||
					d.toTaskId === pendingDeleteTaskId,
			).length
		: 0

	const handleConfirmDelete = () => {
		if (!pendingDeleteTaskId) return
		const idToDelete = pendingDeleteTaskId
		deleteTaskMutation.mutate(
			{ id: idToDelete },
			{
				onSuccess: () => {
					setAllTasks((prev) => prev.filter((t) => t.id !== idToDelete))
					setDependencies((prev) =>
						prev.filter(
							(d) => d.fromTaskId !== idToDelete && d.toTaskId !== idToDelete,
						),
					)
					setPendingDeleteTaskId(null)
				},
			},
		)
	}

	return (
		<DragDropProvider onDragEnd={handleDragEnd}>
			{toastMessage && (
				<Toast message={toastMessage} onClose={() => setToastMessage(null)} />
			)}
			{pendingDeleteTask && (
				<ConfirmDeleteModal
					taskTitle={pendingDeleteTask.title}
					affectedDependenciesCount={affectedDepsCount}
					onConfirm={handleConfirmDelete}
					onCancel={() => setPendingDeleteTaskId(null)}
				/>
			)}
			<div className="flex flex-col h-screen bg-white dark:bg-zinc-950 overflow-hidden">
				{/* View mode toggle */}
				<div className="flex items-center gap-2 px-4 py-2 border-b border-gray-200 dark:border-zinc-800 shrink-0">
					<span className="text-xs text-gray-500 dark:text-zinc-400 mr-1">Масштаб:</span>
					<button
						className={`px-3 py-1 text-xs rounded ${viewMode === 'day' ? 'bg-indigo-100 text-indigo-700 dark:bg-indigo-900 dark:text-indigo-300 font-semibold' : 'text-gray-600 dark:text-zinc-400 hover:bg-gray-100 dark:hover:bg-zinc-800'}`}
						onClick={() => setViewMode('day')}
					>
						День
					</button>
					<button
						className={`px-3 py-1 text-xs rounded ${viewMode === 'week' ? 'bg-indigo-100 text-indigo-700 dark:bg-indigo-900 dark:text-indigo-300 font-semibold' : 'text-gray-600 dark:text-zinc-400 hover:bg-gray-100 dark:hover:bg-zinc-800'}`}
						onClick={() => setViewMode('week')}
					>
						Неделя
					</button>
				</div>

				<div className="flex flex-1 overflow-hidden">
					<div
						ref={leftRef}
						onScroll={syncScroll('left')}
						className="w-105 shrink-0 border-r border-gray-300 dark:border-zinc-700 overflow-y-auto"
					>
						<GanttTaskList
							tasks={allTasks}
							headerHeight={HEADER_HEIGHT}
							rowHeight={ROW_HEIGHT}
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
								weeks={weeks}
								monthGroups={monthGroups}
								dayWidth={dayWidth}
								headerHeight={HEADER_HEIGHT}
								workCalendar={calendar}
								viewMode={viewMode}
							/>
						</div>
						<GanttCalendarGrid
							tasks={allTasks}
							dependencies={dependencies}
							days={days}
							rangeStart={rangeStart}
							dayWidth={dayWidth}
							rowHeight={ROW_HEIGHT}
							timelineCalendar={calendar}
							viewMode={viewMode}
							criticalTaskIds={criticalTaskIds}
							slackMap={slackMap}
							onCreateDependency={handleCreateDependency}
							onChangeDependencyType={handleChangeDependencyType}
							onRemoveDependency={handleRemoveDependency}
							onMoveTask={handleMoveTask}
							onResizeTask={handleResizeTask}
							onResizeFromStart={handleResizeFromStart}
						/>
					</div>
				</div>
			</div>
		</DragDropProvider>
	)
}
