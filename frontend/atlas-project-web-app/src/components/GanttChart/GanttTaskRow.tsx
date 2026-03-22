'use client'

import React, { useRef, useState, useCallback, useEffect } from 'react'
import { GanttTask, TaskCommand } from '@/types'
import { TaskCommandType } from '@/types/types/TaskCommandType'
import { TaskId } from '@/utils/types/TaskId'
import { LocalDate } from '@/utils/types/LocalDate'
import { formatDateForInput } from '@/utils/ganttDateUtils'
import { Calendar } from 'primereact/calendar'
import { addLocale } from 'primereact/api'
import { GripVertical, Trash2, UserPlus } from 'lucide-react'
import { ProjectTaskStatus } from '@/types/generated/enums/project-task-status.enum'

addLocale('ru', {
	firstDayOfWeek: 1,
	dayNames: ['Воскресенье', 'Понедельник', 'Вторник', 'Среда', 'Четверг', 'Пятница', 'Суббота'],
	dayNamesShort: ['Вс', 'Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб'],
	dayNamesMin: ['Вс', 'Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб'],
	monthNames: ['Январь', 'Февраль', 'Март', 'Апрель', 'Май', 'Июнь', 'Июль', 'Август', 'Сентябрь', 'Октябрь', 'Ноябрь', 'Декабрь'],
	monthNamesShort: ['Янв', 'Фев', 'Мар', 'Апр', 'Май', 'Июн', 'Июл', 'Авг', 'Сен', 'Окт', 'Ноя', 'Дек'],
	today: 'Сегодня',
	clear: 'Очистить',
})

interface AssignmentIndicator {
	resourceName: string
	resourceId: string
}

interface GanttTaskRowProps {
	task: GanttTask
	rowHeight: number
	assignments?: AssignmentIndicator[]
	onUpdateTask: (cmd: TaskCommand) => void
	onAssignmentClick?: (taskId: string, e: React.MouseEvent) => void
	dragHandleRef: (element: Element | null) => void
}

const statusColors: Record<ProjectTaskStatus, string> = {
	[ProjectTaskStatus.EMPTY]: 'bg-gray-300 dark:bg-zinc-600',
	[ProjectTaskStatus.BACKLOG]: 'bg-slate-400 dark:bg-slate-600',
	[ProjectTaskStatus.IN_PROGRESS]: 'bg-blue-500 dark:bg-blue-600',
	[ProjectTaskStatus.DONE]: 'bg-emerald-500 dark:bg-emerald-600',
	[ProjectTaskStatus.BLOCKED]: 'bg-red-400 dark:bg-red-600',
}

const statusLabels: Record<ProjectTaskStatus, string> = {
	[ProjectTaskStatus.EMPTY]: 'Пусто',
	[ProjectTaskStatus.BACKLOG]: 'Бэклог',
	[ProjectTaskStatus.IN_PROGRESS]: 'В работе',
	[ProjectTaskStatus.DONE]: 'Готово',
	[ProjectTaskStatus.BLOCKED]: 'Заблокировано',
}

type CalendarField = 'start' | 'end' | 'actualStart' | 'actualEnd'

const fmtDate = (date?: Date) => {
	if (!date) return '—'
	return date.toLocaleDateString('ru-RU', { day: '2-digit', month: '2-digit' })
}

function GanttTaskRow({
	task,
	rowHeight,
	assignments,
	onUpdateTask,
	onAssignmentClick,
	dragHandleRef,
}: GanttTaskRowProps) {
	const [isEditing, setIsEditing] = useState(false)
	const [editTitle, setEditTitle] = useState(task.title)
	const [editDescription, setEditDescription] = useState(task.description)
	const [showStatusMenu, setShowStatusMenu] = useState(false)
	const [isEditingEffort, setIsEditingEffort] = useState(false)
	const [editBaseline, setEditBaseline] = useState(String(task.baselineEffortHours ?? ''))
	const [editAdditional, setEditAdditional] = useState(String(task.additionalEffortHours ?? ''))
	const [openCalendar, setOpenCalendar] = useState<CalendarField | null>(null)
	const calRef = useRef<Calendar>(null)
	const statusMenuRef = useRef<HTMLDivElement>(null)

	const isPoolTask = !task.start && !task.end

	// Auto-show calendar when it mounts
	useEffect(() => {
		if (openCalendar && calRef.current) {
			calRef.current.show()
		}
	}, [openCalendar])

	const handleTitleSubmit = useCallback(() => {
		setIsEditing(false)
		if (editTitle.trim() && editTitle !== task.title) {
			onUpdateTask({ type: TaskCommandType.UpdateTitle, taskId: TaskId(task.id), newTitle: editTitle.trim() })
		} else {
			setEditTitle(task.title)
		}
		if (editDescription !== task.description) {
			onUpdateTask({ type: TaskCommandType.UpdateDescription, taskId: TaskId(task.id), newDescription: editDescription })
		}
	}, [editTitle, editDescription, task.title, task.description, task.id, onUpdateTask])

	const handleCalendarChange = useCallback((value: Date | null | undefined) => {
		const field = openCalendar
		setOpenCalendar(null)
		if (!field) return

		if (field === 'start' && value) {
			onUpdateTask({ type: TaskCommandType.ChangeStartDate, taskId: TaskId(task.id), newStartDate: LocalDate(formatDateForInput(value)) })
		} else if (field === 'end' && value) {
			if (isPoolTask) {
				onUpdateTask({ type: TaskCommandType.PlanFromEnd, taskId: TaskId(task.id), newEndDate: LocalDate(formatDateForInput(value)) })
			} else {
				onUpdateTask({ type: TaskCommandType.ChangeEndDate, taskId: TaskId(task.id), newEndDate: LocalDate(formatDateForInput(value)) })
			}
		} else if (field === 'actualStart') {
			onUpdateTask({
				type: TaskCommandType.UpdateFields,
				taskId: TaskId(task.id),
				updates: { actualStartDate: value ? formatDateForInput(value) : null },
			})
		} else if (field === 'actualEnd') {
			onUpdateTask({
				type: TaskCommandType.UpdateFields,
				taskId: TaskId(task.id),
				updates: { actualEndDate: value ? formatDateForInput(value) : null },
			})
		}
	}, [openCalendar, isPoolTask, task.id, onUpdateTask])

	const handleEffortSubmit = useCallback(() => {
		setIsEditingEffort(false)
		const baseVal = editBaseline.trim() === '' ? null : parseFloat(editBaseline)
		const addVal = editAdditional.trim() === '' ? null : parseFloat(editAdditional)
		const baseChanged = baseVal !== (task.baselineEffortHours ?? null)
		const addChanged = addVal !== (task.additionalEffortHours ?? null)
		if (baseChanged || addChanged) {
			onUpdateTask({
				type: TaskCommandType.UpdateFields,
				taskId: TaskId(task.id),
				updates: {
					...(baseChanged ? { baselineEffortHours: baseVal } : {}),
					...(addChanged ? { additionalEffortHours: addVal } : {}),
				},
			})
		}
	}, [editBaseline, editAdditional, task.baselineEffortHours, task.additionalEffortHours, task.id, onUpdateTask])

	const handleStatusChange = useCallback((newStatus: ProjectTaskStatus) => {
		setShowStatusMenu(false)
		if (newStatus !== task.status) {
			onUpdateTask({ type: TaskCommandType.ChangeStatus, taskId: TaskId(task.id), newStatus })
		}
	}, [task.status, task.id, onUpdateTask])

	// Resolve which date value to pass to the single lazy Calendar
	const calendarValue = openCalendar === 'start' ? (task.start ?? null)
		: openCalendar === 'end' ? (task.end ?? null)
		: openCalendar === 'actualStart' ? (task.actualStart ?? null)
		: openCalendar === 'actualEnd' ? (task.actualEnd ?? null)
		: null

	return (
		<div
			className="group flex gap-1.5 px-3 py-0.5 border-b border-gray-200 dark:border-zinc-800"
			style={{ height: rowHeight }}
		>
			{/* Left column: drag + status */}
			<div className="flex items-center gap-1.5 shrink-0 self-center">
				<div
					ref={dragHandleRef}
					className="cursor-grab active:cursor-grabbing text-gray-300 dark:text-zinc-600 hover:text-gray-500 dark:hover:text-zinc-400 touch-none"
				>
					<GripVertical size={14} />
				</div>
				<div className="relative">
					<div
						className={`w-2.5 h-2.5 rounded-full cursor-pointer ${statusColors[task.status]}`}
						onClick={() => setShowStatusMenu(!showStatusMenu)}
						title={statusLabels[task.status]}
					/>
					{showStatusMenu && (
						<div
							ref={statusMenuRef}
							className="absolute left-0 top-5 z-30 bg-white dark:bg-zinc-900 rounded-md shadow-lg border border-gray-200 dark:border-zinc-700 py-1 min-w-36"
							onMouseLeave={() => setShowStatusMenu(false)}
						>
							{Object.values(ProjectTaskStatus).map((s) => (
								<button
									key={s}
									onClick={() => handleStatusChange(s)}
									className={`flex items-center gap-2 w-full px-3 py-1.5 text-xs hover:bg-gray-100 dark:hover:bg-zinc-800 text-left ${
										s === task.status ? 'font-semibold' : ''
									}`}
								>
									<div className={`w-2 h-2 rounded-full ${statusColors[s]}`} />
									<span className="text-gray-700 dark:text-zinc-300">{statusLabels[s]}</span>
								</button>
							))}
						</div>
					)}
				</div>
			</div>

			{/* Right: two rows */}
			<div className="flex-1 min-w-0 flex flex-col justify-center gap-0">
				{/* Row 1: Title + actions */}
				<div className="flex items-center gap-1.5 min-w-0">
					{isEditing ? (
						<div className="flex-1 min-w-0 flex flex-col gap-0.5">
							<input
								type="text"
								value={editTitle}
								onChange={(e) => setEditTitle(e.target.value)}
								onBlur={handleTitleSubmit}
								onKeyDown={(e) => {
									if (e.key === 'Enter') handleTitleSubmit()
									if (e.key === 'Escape') {
										setEditTitle(task.title)
										setEditDescription(task.description)
										setIsEditing(false)
									}
								}}
								className="w-full text-sm bg-transparent border-b border-blue-500 outline-none text-gray-900 dark:text-white py-0"
								autoFocus
							/>
							<input
								type="text"
								value={editDescription}
								onChange={(e) => setEditDescription(e.target.value)}
								onBlur={handleTitleSubmit}
								onKeyDown={(e) => {
									if (e.key === 'Enter') handleTitleSubmit()
									if (e.key === 'Escape') {
										setEditTitle(task.title)
										setEditDescription(task.description)
										setIsEditing(false)
									}
								}}
								placeholder="Описание..."
								className="w-full text-[10px] bg-transparent border-b border-gray-300 dark:border-zinc-600 outline-none text-gray-500 dark:text-zinc-400 py-0"
							/>
						</div>
					) : (
						<div
							className="flex-1 min-w-0 cursor-pointer"
							onClick={() => {
								setEditTitle(task.title)
								setEditDescription(task.description)
								setIsEditing(true)
							}}
						>
							<span
								className="text-sm text-gray-900 dark:text-white truncate block leading-tight hover:text-blue-600 dark:hover:text-blue-400"
								title={task.title}
							>
								{task.title || 'Без названия'}
							</span>
						</div>
					)}

					{/* Assignment indicators */}
					<div className="flex items-center gap-0.5 shrink-0">
						{assignments && assignments.length > 0 && (
							<div className="flex items-center gap-0.5">
								{assignments.slice(0, 2).map((a) => (
									<span
										key={a.resourceId}
										className="w-4 h-4 rounded-full bg-indigo-100 text-[8px] text-indigo-700 font-semibold flex items-center justify-center"
										title={a.resourceName}
									>
										{a.resourceName.charAt(0).toUpperCase()}
									</span>
								))}
								{assignments.length > 2 && (
									<span className="text-[8px] text-gray-400">+{assignments.length - 2}</span>
								)}
							</div>
						)}
						<button
							onClick={(e) => onAssignmentClick?.(task.id, e)}
							className="opacity-0 group-hover:opacity-100 text-gray-400 hover:text-indigo-600 transition-opacity"
							title="Назначить ресурс"
						>
							<UserPlus size={12} />
						</button>
					</div>

					<button
						onClick={() => onUpdateTask({ type: TaskCommandType.DeleteTask, taskId: TaskId(task.id) })}
						className="opacity-0 group-hover:opacity-100 shrink-0 text-gray-400 hover:text-red-500 transition-opacity"
					>
						<Trash2 size={12} />
					</button>
				</div>

				{/* Row 2: Dates + effort (compact) */}
				<div className="flex items-center gap-1 text-[10px] tabular-nums">
					{/* Plan dates */}
					<span
						onClick={() => setOpenCalendar('start')}
						className="text-gray-500 dark:text-zinc-400 cursor-pointer hover:text-blue-600 dark:hover:text-blue-400"
					>
						{fmtDate(task.start ?? undefined)}
					</span>
					<span className="text-gray-300 dark:text-zinc-700">–</span>
					<span
						onClick={() => setOpenCalendar('end')}
						className="text-gray-500 dark:text-zinc-400 cursor-pointer hover:text-blue-600 dark:hover:text-blue-400"
					>
						{fmtDate(task.end ?? undefined)}
					</span>

					<span className="text-gray-200 dark:text-zinc-800 mx-0.5">|</span>

					{/* Actual dates */}
					<span
						onClick={() => setOpenCalendar('actualStart')}
						className={`cursor-pointer hover:text-emerald-600 dark:hover:text-emerald-400 ${
							task.actualStart ? 'text-emerald-600 dark:text-emerald-400' : 'text-gray-300 dark:text-zinc-600'
						}`}
						title="Факт. начало"
					>
						{task.actualStart ? fmtDate(task.actualStart) : 'ф.н'}
					</span>
					<span className="text-gray-300 dark:text-zinc-700">–</span>
					<span
						onClick={() => setOpenCalendar('actualEnd')}
						className={`cursor-pointer hover:text-emerald-600 dark:hover:text-emerald-400 ${
							task.actualEnd ? 'text-emerald-600 dark:text-emerald-400' : 'text-gray-300 dark:text-zinc-600'
						}`}
						title="Факт. окончание"
					>
						{task.actualEnd ? fmtDate(task.actualEnd) : 'ф.о'}
					</span>

					<span className="text-gray-200 dark:text-zinc-800 mx-0.5">|</span>

					{/* Effort */}
					{isEditingEffort ? (
						<div className="flex items-center gap-0.5">
							<input
								type="number"
								value={editBaseline}
								onChange={(e) => setEditBaseline(e.target.value)}
								onBlur={handleEffortSubmit}
								onKeyDown={(e) => {
									if (e.key === 'Enter') handleEffortSubmit()
									if (e.key === 'Escape') {
										setEditBaseline(String(task.baselineEffortHours ?? ''))
										setEditAdditional(String(task.additionalEffortHours ?? ''))
										setIsEditingEffort(false)
									}
								}}
								placeholder="Баз"
								title="Базовая трудоёмкость (ч)"
								className="w-8 bg-transparent border-b border-blue-500 outline-none text-gray-900 dark:text-white text-center text-[10px]"
								autoFocus
							/>
							<span className="text-gray-400">+</span>
							<input
								type="number"
								value={editAdditional}
								onChange={(e) => setEditAdditional(e.target.value)}
								onBlur={handleEffortSubmit}
								onKeyDown={(e) => {
									if (e.key === 'Enter') handleEffortSubmit()
									if (e.key === 'Escape') {
										setEditBaseline(String(task.baselineEffortHours ?? ''))
										setEditAdditional(String(task.additionalEffortHours ?? ''))
										setIsEditingEffort(false)
									}
								}}
								placeholder="Доп"
								title="Дополнительная трудоёмкость (ч)"
								className="w-8 bg-transparent border-b border-blue-500 outline-none text-gray-900 dark:text-white text-center text-[10px]"
							/>
							<span className="text-gray-400">ч</span>
						</div>
					) : (
						<span
							onClick={() => {
								setEditBaseline(String(task.baselineEffortHours ?? ''))
								setEditAdditional(String(task.additionalEffortHours ?? ''))
								setIsEditingEffort(true)
							}}
							className={`cursor-pointer hover:text-blue-600 dark:hover:text-blue-400 ${
								task.baselineEffortHours != null
									? (task.effortCoveragePercent ?? 0) >= 100
										? 'text-emerald-600 dark:text-emerald-400'
										: (task.effortCoveragePercent ?? 0) >= 50
											? 'text-amber-600 dark:text-amber-400'
											: 'text-red-500 dark:text-red-400'
									: 'text-gray-300 dark:text-zinc-600'
							}`}
							title={
								task.baselineEffortHours != null
									? `Покрытие: ${task.allocatedEffortHours ?? 0}ч / ${(task.baselineEffortHours ?? 0) + (task.additionalEffortHours ?? 0)}ч`
									: 'Задать трудоёмкость'
							}
						>
							{task.baselineEffortHours != null
								? `${task.allocatedEffortHours ?? 0}/${(task.baselineEffortHours ?? 0) + (task.additionalEffortHours ?? 0)}ч`
								: 'трд'}
						</span>
					)}

					{/* Single lazy Calendar — only mounts when a date field is clicked */}
					{openCalendar && (
						<div className="relative">
							<Calendar
								ref={calRef}
								value={calendarValue}
								onChange={(e) => handleCalendarChange(e.value as Date | null)}
								onHide={() => setOpenCalendar(null)}
								dateFormat="dd.mm.yy"
								locale="ru"
								inputClassName="!w-0 !h-0 !p-0 !border-0 !opacity-0 !absolute"
								panelClassName="gantt-date-panel"
							/>
						</div>
					)}
				</div>
			</div>
		</div>
	)
}

export default React.memo(GanttTaskRow)
