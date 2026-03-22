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
	useReorderTasks,
	useSaveBaseline,
} from '@/hooks/useProjectTasks'
import { GanttTask, GanttDependencyDto, TaskCommand } from '@/types'
import { ProjectTaskStatus } from '@/types/generated/enums/project-task-status.enum'
import {
	getCalendarRange,
	getDaysInRange,
	getDayOffset,
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
import AnalysisPanel from './AnalysisPanel'
import ResourceLoadPanel, { LOAD_ROW_HEIGHT, type TaskBreakdownMap } from './ResourceLoadPanel'
import Toast from './Toast'
import { TaskCommandType } from '@/types/types/TaskCommandType'
import { TaskId } from '@/utils/types/TaskId'
import { LocalDate } from '@/utils/types/LocalDate'
import { BarChart3, Users, Activity, Gauge, Filter, Bookmark } from 'lucide-react'
import Link from 'next/link'
import { useAssignments, useResourceLoad } from '@/hooks/useAssignments'
import { useGlobalResourceLoad } from '@/hooks/useCrossProjectLoad'
import { useResources } from '@/hooks/useResources'
import { AssignmentEditor } from '@/components/Assignments/AssignmentEditor'
import { DragDropProvider } from '@dnd-kit/react'
import type { DragEndEvent } from '@dnd-kit/react'
import { isSortableOperation } from '@dnd-kit/react/sortable'

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

export const GanttChart = ({ projectId }: { projectId: string }) => {
	const viewMode = useTimelineCalendarStore((s) => s.ui.viewMode)
	const setViewMode = useTimelineCalendarStore((s) => s.setViewMode)
	const isAnalysisPanelOpen = useTimelineCalendarStore((s) => s.ui.isAnalysisPanelOpen)
	const setAnalysisPanelOpen = useTimelineCalendarStore((s) => s.setAnalysisPanelOpen)
	const showResourceLoad = useTimelineCalendarStore((s) => s.ui.showResourceLoad)
	const toggleResourceLoad = useTimelineCalendarStore((s) => s.toggleResourceLoad)
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
	} = useProjectPlan(projectId)

	const [allTasks, setAllTasks] = useState<GanttTask[]>([])
	const [dependencies, setDependencies] = useState<GanttDependencyDto[]>([])
	const prevTasksRef = useRef<GanttTask[]>([])

	const createTaskMutation = useCreateProjectTask(projectId)
	const changeStartMutation = useChangeTaskStartDate(projectId)
	const resizeFromStartMutation = useResizeTaskFromStart(projectId)
	const changeEndMutation = useChangeTaskEndDate(projectId)
	const createDependencyMutation = useCreateDependency(projectId)
	const changeDependencyTypeMutation = useChangeDependencyType(projectId)
	const deleteDependencyMutation = useDeleteDependency(projectId)
	const updateTitleMutation = useUpdateProjectTask(projectId)
	const deleteTaskMutation = useDeleteProjectTask(projectId)
	const assignScheduleMutation = useAssignTaskSchedule(projectId)
	const planFromEndMutation = usePlanFromEnd(projectId)
	const reorderTasksMutation = useReorderTasks(projectId)
	const saveBaselineMutation = useSaveBaseline(projectId)
	const { data: criticalPathData } = useCriticalPath(projectId)
	const { data: assignments } = useAssignments(projectId)
	const { data: resources } = useResources()

	const [assignmentEditor, setAssignmentEditor] = useState<{
		taskId: string
		taskTitle: string
		position: { x: number; y: number }
	} | null>(null)

	const assignmentsByTask = useMemo(() => {
		if (!assignments || !resources) return new Map<string, Array<{ resourceName: string; resourceId: string }>>()
		const resourceMap = new Map(resources.map((r) => [r.id, r.name]))
		const map = new Map<string, Array<{ resourceName: string; resourceId: string }>>()
		for (const a of assignments) {
			const list = map.get(a.taskId) ?? []
			list.push({ resourceId: a.resourceId, resourceName: resourceMap.get(a.resourceId) ?? '?' })
			map.set(a.taskId, list)
		}
		return map
	}, [assignments, resources])

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

	// Resource load for inline Gantt visualization
	const loadDateRange = useMemo(() => {
		const tasksWithDates = allTasks.filter((t) => t.start && t.end)
		if (tasksWithDates.length === 0) return { from: null, to: null }
		const { start, end } = getCalendarRange(tasksWithDates)
		return { from: formatDateForInput(start), to: formatDateForInput(end) }
	}, [allTasks])

	const { data: resourceLoadReport } = useResourceLoad(
		projectId,
		showResourceLoad ? loadDateRange.from : null,
		showResourceLoad ? loadDateRange.to : null,
	)

	const { data: globalLoadReport } = useGlobalResourceLoad(
		showResourceLoad ? loadDateRange.from : null,
		showResourceLoad ? loadDateRange.to : null,
	)

	// Build cross-project map: resourceId → date → { thisProject, otherProjects, capacity, otherProjectDetails }
	const crossProjectMap = useMemo(() => {
		if (!globalLoadReport) return undefined
		const map = new Map<string, Map<string, { thisProject: number; otherProjects: number; capacity: number; otherProjectDetails: { projectId: string; projectName: string; hours: number }[] }>>()
		for (const res of globalLoadReport.resources) {
			const dayMap = new Map<string, { thisProject: number; otherProjects: number; capacity: number; otherProjectDetails: { projectId: string; projectName: string; hours: number }[] }>()
			for (const day of res.days) {
				let thisProjectHours = 0
				let otherProjectsHours = 0
				const otherProjectDetails: { projectId: string; projectName: string; hours: number }[] = []
				for (const contrib of day.projectBreakdown) {
					if (contrib.projectId === projectId) {
						thisProjectHours += contrib.hours
					} else {
						otherProjectsHours += contrib.hours
						otherProjectDetails.push({ projectId: contrib.projectId, projectName: contrib.projectName, hours: contrib.hours })
					}
				}
				dayMap.set(day.date, {
					thisProject: thisProjectHours,
					otherProjects: otherProjectsHours,
					capacity: day.capacityHours,
					otherProjectDetails,
				})
			}
			map.set(res.resourceId, dayMap)
		}
		return map
	}, [globalLoadReport, projectId])

	// Build task breakdown map: resourceId → date → [{ taskId, taskTitle, hours }]
	const taskBreakdownMap: TaskBreakdownMap | undefined = useMemo(() => {
		if (!assignments || assignments.length === 0) return undefined
		const taskMap = new Map(allTasks.map((t) => [t.id, t]))
		const map: TaskBreakdownMap = new Map()
		for (const a of assignments) {
			const task = taskMap.get(a.taskId)
			if (!task || !task.start || !task.end) continue
			const startMs = task.start.getTime()
			const endMs = task.end.getTime()
			let resDayMap = map.get(a.resourceId)
			if (!resDayMap) {
				resDayMap = new Map()
				map.set(a.resourceId, resDayMap)
			}
			// Iterate each day in task range
			for (let ms = startMs; ms <= endMs; ms += 86400000) {
				const d = new Date(ms)
				const y = d.getFullYear()
				const m = String(d.getMonth() + 1).padStart(2, '0')
				const day = String(d.getDate()).padStart(2, '0')
				const dateStr = `${y}-${m}-${day}`
				let list = resDayMap.get(dateStr)
				if (!list) {
					list = []
					resDayMap.set(dateStr, list)
				}
				list.push({ taskId: task.id, taskTitle: task.title, hours: a.hoursPerDay })
			}
		}
		return map
	}, [assignments, allTasks])

	// Resource load filter: only project-assigned resources, user can toggle individual ones
	const projectResourceIds = useMemo(() => {
		if (!assignments) return new Set<string>()
		return new Set(assignments.map((a) => a.resourceId))
	}, [assignments])

	const [hiddenResourceIds, setHiddenResourceIds] = useState<Set<string>>(new Set())
	const [showResourceFilter, setShowResourceFilter] = useState(false)

	const filteredLoadReport = useMemo(() => {
		if (!resourceLoadReport) return null
		const filtered = resourceLoadReport.resources
			.filter((r) => projectResourceIds.has(r.resourceId))
			.filter((r) => !hiddenResourceIds.has(r.resourceId))
		return { ...resourceLoadReport, resources: filtered }
	}, [resourceLoadReport, projectResourceIds, hiddenResourceIds])

	// All project resources (for filter UI)
	const projectResources = useMemo(() => {
		if (!resourceLoadReport) return []
		return resourceLoadReport.resources.filter((r) => projectResourceIds.has(r.resourceId))
	}, [resourceLoadReport, projectResourceIds])

	const [pendingDeleteTaskId, setPendingDeleteTaskId] = useState<string | null>(
		null,
	)
	const [toastMessage, setToastMessage] = useState<string | null>(null)
	const leftRef = useRef<HTMLDivElement>(null)
	const rightRef = useRef<HTMLDivElement>(null)
	const loadPanelRef = useRef<HTMLDivElement>(null)
	const isSyncing = useRef(false)
	const hasScrolledToToday = useRef(false)

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

				case TaskCommandType.UpdateFields:
					updateTitleMutation.mutate(
						{ id: cmd.taskId, updates: cmd.updates },
						{ onError: () => setToastMessage('Не удалось обновить поля задачи') },
					)
					break

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

	const handleReorder = useCallback(
		(orderedIds: string[]) => {
			setAllTasks((prev) => {
				prevTasksRef.current = prev
				const idToTask = new Map(prev.map((t) => [t.id, t]))
				return orderedIds.map((id) => idToTask.get(id)).filter(Boolean) as GanttTask[]
			})
			reorderTasksMutation.mutate(
				{ orderedIds },
				{
					onError: () => {
						setAllTasks(prevTasksRef.current)
						setToastMessage('Не удалось изменить порядок задач')
					},
				},
			)
		},
		[reorderTasksMutation],
	)

	const handleDragEnd = useCallback(
		(event: DragEndArg) => {
			if (event.canceled) return
			const { operation } = event
			if (!operation.source || !operation.target) return

			// Handle sortable reorder
			if (isSortableOperation(operation)) {
				const sourceData = operation.source.data as { type?: string; taskId?: string } | undefined
				if (sourceData?.type === 'reorder') {
					const sourceIndex = operation.source.initialIndex
					const targetIndex = operation.source.index
					if (sourceIndex != null && targetIndex != null && sourceIndex !== targetIndex) {
						const newOrder = [...allTasks]
						const [moved] = newOrder.splice(sourceIndex, 1)
						newOrder.splice(targetIndex, 0, moved)
						handleReorder(newOrder.map((t) => t.id))
					}
					return
				}
			}

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
		[allTasks, planData, handleUpdateTask, handleReorder],
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
			// Sync horizontal scroll to load panel
			if (source === 'right' && rightRef.current && loadPanelRef.current) {
				loadPanelRef.current.scrollLeft = rightRef.current.scrollLeft
			}
			requestAnimationFrame(() => {
				isSyncing.current = false
			})
		},
		[],
	)

	// Close resource filter dropdown on outside click
	const resourceFilterRef = useRef<HTMLDivElement>(null)
	useEffect(() => {
		if (!showResourceFilter) return
		const handleClick = (e: MouseEvent) => {
			if (resourceFilterRef.current && !resourceFilterRef.current.contains(e.target as Node)) {
				setShowResourceFilter(false)
			}
		}
		document.addEventListener('mousedown', handleClick)
		return () => document.removeEventListener('mousedown', handleClick)
	}, [showResourceFilter])

	// Reset scroll-to-today when view mode changes
	useEffect(() => {
		hasScrolledToToday.current = false
	}, [viewMode])

	// Scroll to today on initial load
	useEffect(() => {
		if (hasScrolledToToday.current) return
		if (!planData || allTasks.length === 0) return
		const container = rightRef.current
		if (!container) return

		const tasksWithDates = allTasks.filter((t) => t.start && t.end)
		if (tasksWithDates.length === 0) return

		const { start: rawStart } = getCalendarRange(tasksWithDates)
		const rs = viewMode === 'week' ? alignToMonday(rawStart) : rawStart
		const todayOffset = getDayOffset(new Date(), rs)
		const todayPx = todayOffset * dayWidth
		const centerPx = todayPx - container.clientWidth / 2

		container.scrollLeft = Math.max(0, centerPx)
		hasScrolledToToday.current = true
	}, [planData, allTasks, dayWidth, viewMode])

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

					<div className="ml-auto flex items-center gap-2">
						<Link
							href={`/projects/${projectId}/resources`}
							className="flex items-center gap-1 rounded px-3 py-1 text-xs text-gray-600 hover:bg-gray-100 dark:text-zinc-400 dark:hover:bg-zinc-800"
						>
							<Users size={14} />
							Ресурсы
						</Link>
						<Link
							href={`/projects/${projectId}/resource-load`}
							className="flex items-center gap-1 rounded px-3 py-1 text-xs text-gray-600 hover:bg-gray-100 dark:text-zinc-400 dark:hover:bg-zinc-800"
						>
							<Activity size={14} />
							Нагрузка
						</Link>
						<button
							className={`flex items-center gap-1 px-3 py-1 text-xs rounded ${showResourceLoad ? 'bg-indigo-100 text-indigo-700 dark:bg-indigo-900 dark:text-indigo-300 font-semibold' : 'text-gray-600 dark:text-zinc-400 hover:bg-gray-100 dark:hover:bg-zinc-800'}`}
							onClick={toggleResourceLoad}
						>
							<Gauge size={14} />
							Загрузка
						</button>
						<button
							className={`flex items-center gap-1 px-3 py-1 text-xs rounded ${isAnalysisPanelOpen ? 'bg-indigo-100 text-indigo-700 dark:bg-indigo-900 dark:text-indigo-300 font-semibold' : 'text-gray-600 dark:text-zinc-400 hover:bg-gray-100 dark:hover:bg-zinc-800'}`}
							onClick={() => setAnalysisPanelOpen(!isAnalysisPanelOpen)}
						>
							<BarChart3 size={14} />
							Аналитика
						</button>
						<button
							className="flex items-center gap-1 px-3 py-1 text-xs rounded text-gray-600 dark:text-zinc-400 hover:bg-gray-100 dark:hover:bg-zinc-800"
							onClick={() => {
								if (confirm('Сохранить текущие даты как базовый план? Предыдущий базовый план будет перезаписан.')) {
									saveBaselineMutation.mutate(undefined)
								}
							}}
						>
							<Bookmark size={14} />
							Базовый план
						</button>
					</div>
				</div>

				<div className="flex flex-col flex-1 overflow-hidden min-h-0">
					<div className="flex flex-1 overflow-hidden min-h-0">
						<div
							ref={leftRef}
							onScroll={syncScroll('left')}
							className="w-105 shrink-0 border-r border-gray-300 dark:border-zinc-700 overflow-y-auto"
						>
							<GanttTaskList
								tasks={allTasks}
								headerHeight={HEADER_HEIGHT}
								rowHeight={ROW_HEIGHT}
								assignmentsByTask={assignmentsByTask}
								onUpdateTask={handleUpdateTask}
								onReorder={handleReorder}
								onAssignmentClick={(taskId, e) => {
									const task = allTasks.find((t) => t.id === taskId)
									if (!task) return
									setAssignmentEditor({
										taskId,
										taskTitle: task.title,
										position: { x: e.clientX, y: e.clientY },
									})
								}}
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
								assignmentsByTask={assignmentsByTask}
								onCreateDependency={handleCreateDependency}
								onChangeDependencyType={handleChangeDependencyType}
								onRemoveDependency={handleRemoveDependency}
								onMoveTask={handleMoveTask}
								onResizeTask={handleResizeTask}
								onResizeFromStart={handleResizeFromStart}
								onAssignmentClick={(taskId, e) => {
									const task = allTasks.find((t) => t.id === taskId)
									if (!task) return
									setAssignmentEditor({
										taskId,
										taskTitle: task.title,
										position: { x: e.clientX, y: e.clientY },
									})
								}}
							/>
						</div>

						{isAnalysisPanelOpen && (
							<div className="w-80 shrink-0 border-l border-gray-300 dark:border-zinc-700 overflow-y-auto bg-white dark:bg-zinc-950">
								<AnalysisPanel
									projectId={projectId}
									tasks={allTasks}
									onClose={() => setAnalysisPanelOpen(false)}
								/>
							</div>
						)}
					</div>

					{/* Resource load panel */}
					{showResourceLoad && filteredLoadReport && filteredLoadReport.resources.length > 0 && (
						<div className="shrink-0 border-t border-gray-300 dark:border-zinc-700 flex max-h-[40vh] overflow-y-auto">
							{/* Resource labels */}
							<div className="w-105 shrink-0 border-r border-gray-300 dark:border-zinc-700 bg-gray-50 dark:bg-zinc-900">
								{/* Filter header */}
								<div ref={resourceFilterRef} className="relative flex items-center px-3 h-7 border-b border-gray-300 dark:border-zinc-700">
									<span className="text-[10px] text-gray-500 dark:text-zinc-500 uppercase tracking-wide">Ресурсы</span>
									<button
										className="ml-auto p-0.5 rounded hover:bg-gray-200 dark:hover:bg-zinc-700"
										onClick={() => setShowResourceFilter((v) => !v)}
										title="Фильтр ресурсов"
									>
										<Filter size={12} className={hiddenResourceIds.size > 0 ? 'text-indigo-500' : 'text-gray-400 dark:text-zinc-500'} />
									</button>
									{showResourceFilter && (
										<div className="absolute left-0 top-full z-50 w-64 bg-white dark:bg-zinc-800 border border-gray-200 dark:border-zinc-700 rounded shadow-lg py-1 max-h-60 overflow-y-auto">
											{projectResources.map((r) => (
												<label
													key={r.resourceId}
													className="flex items-center gap-2 px-3 py-1.5 text-xs hover:bg-gray-50 dark:hover:bg-zinc-700 cursor-pointer"
												>
													<input
														type="checkbox"
														checked={!hiddenResourceIds.has(r.resourceId)}
														onChange={() => {
															setHiddenResourceIds((prev) => {
																const next = new Set(prev)
																if (next.has(r.resourceId)) next.delete(r.resourceId)
																else next.add(r.resourceId)
																return next
															})
														}}
														className="rounded border-gray-300 dark:border-zinc-600"
													/>
													<span className="truncate text-gray-700 dark:text-zinc-300">{r.resourceName}</span>
													{r.overloadedDaysCount > 0 && (
														<span className="ml-auto text-[10px] text-red-500">{r.overloadedDaysCount}д</span>
													)}
												</label>
											))}
											{hiddenResourceIds.size > 0 && (
												<button
													className="w-full px-3 py-1.5 text-xs text-indigo-600 dark:text-indigo-400 hover:bg-gray-50 dark:hover:bg-zinc-700 text-left border-t border-gray-100 dark:border-zinc-700"
													onClick={() => setHiddenResourceIds(new Set())}
												>
													Показать все
												</button>
											)}
										</div>
									)}
								</div>
								{filteredLoadReport.resources.map((r) => (
									<div
										key={r.resourceId}
										className="flex items-center px-3 text-xs text-gray-600 dark:text-zinc-400 truncate border-b border-gray-200 dark:border-zinc-800"
										style={{ height: LOAD_ROW_HEIGHT }}
									>
										<span className="truncate">{r.resourceName}</span>
										{r.overloadedDaysCount > 0 && (
											<span className="ml-auto shrink-0 text-[10px] text-red-500 font-medium">
												{r.overloadedDaysCount}д
											</span>
										)}
									</div>
								))}
							</div>
							{/* Load bars */}
							<div ref={loadPanelRef} className="flex-1 overflow-x-hidden bg-gray-50/50 dark:bg-zinc-900/50">
								<div className="h-7 border-b border-gray-300 dark:border-zinc-700" />
								<ResourceLoadPanel
									report={filteredLoadReport}
									days={days}
									dayWidth={dayWidth}
									crossProjectMap={crossProjectMap}
									taskBreakdownMap={taskBreakdownMap}
								/>
							</div>
						</div>
					)}
					{showResourceLoad && filteredLoadReport && filteredLoadReport.resources.length === 0 && projectResourceIds.size > 0 && hiddenResourceIds.size > 0 && (
						<div className="shrink-0 border-t border-gray-300 dark:border-zinc-700 px-4 py-3 text-xs text-gray-500 dark:text-zinc-400 text-center">
							Все ресурсы скрыты фильтром.{' '}
							<button className="text-indigo-600 dark:text-indigo-400 hover:underline" onClick={() => setHiddenResourceIds(new Set())}>
								Показать все
							</button>
						</div>
					)}
				</div>
			</div>
			{assignmentEditor && (
				<AssignmentEditor
					projectId={projectId}
					taskId={assignmentEditor.taskId}
					taskTitle={assignmentEditor.taskTitle}
					position={assignmentEditor.position}
					onClose={() => setAssignmentEditor(null)}
				/>
			)}
		</DragDropProvider>
	)
}
