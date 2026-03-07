'use client'

import { useRef, useState } from 'react'
import { GanttTask, TaskCommand } from '@/types'
import { TaskCommandType } from '@/types/types/TaskCommandType'
import { TaskId } from '@/utils/types/TaskId'
import { LocalDate } from '@/utils/types/LocalDate'
import { formatDateForInput } from '@/utils/ganttDateUtils'
import { Calendar } from 'primereact/calendar'
import { addLocale } from 'primereact/api'
import { Trash2 } from 'lucide-react'
import { ProjectTaskStatus } from '@/types/generated/enums/project-task-status.enum'
import { useDraggable } from '@dnd-kit/react'

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

interface GanttTaskRowProps {
	task: GanttTask
	rowHeight: number
	onUpdateTask: (cmd: TaskCommand) => void
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

export default function GanttTaskRow({
	task,
	rowHeight,
	onUpdateTask,
}: GanttTaskRowProps) {
	const [isEditing, setIsEditing] = useState(false)
	const [editTitle, setEditTitle] = useState(task.title)
	const [editDescription, setEditDescription] = useState(task.description)
	const [showStatusMenu, setShowStatusMenu] = useState(false)
	const startCalRef = useRef<Calendar>(null)
	const endCalRef = useRef<Calendar>(null)
	const elementRef = useRef<HTMLDivElement>(null)
	const statusMenuRef = useRef<HTMLDivElement>(null)

	const isPoolTask = !task.start && !task.end

	const { ref: dragRef, isDragging } = useDraggable({
		id: task.id,
		element: elementRef,
		data: { taskId: task.id },
		disabled: !isPoolTask,
	})

	const formatDate = (date?: Date) => {
		if (!date) return '—'
		return date.toLocaleDateString('ru-RU', { day: '2-digit', month: '2-digit', year: 'numeric' })
	}

	const handleTitleSubmit = () => {
		setIsEditing(false)
		if (editTitle.trim() && editTitle !== task.title) {
			onUpdateTask({ type: TaskCommandType.UpdateTitle, taskId: TaskId(task.id), newTitle: editTitle.trim() })
		} else {
			setEditTitle(task.title)
		}
		if (editDescription !== task.description) {
			onUpdateTask({ type: TaskCommandType.UpdateDescription, taskId: TaskId(task.id), newDescription: editDescription })
		}
	}

	const handleStartDateChange = (value: Date | null | undefined) => {
		if (value !== null && value !== undefined) {
			onUpdateTask({ type: TaskCommandType.ChangeStartDate, taskId: TaskId(task.id), newStartDate: LocalDate(formatDateForInput(value)) })
		}
	}

	const handleEndDateChange = (value: Date | null | undefined) => {
		if (value !== null && value !== undefined) {
			if (isPoolTask) {
				// Pool task: plan backwards from end date
				onUpdateTask({ type: TaskCommandType.PlanFromEnd, taskId: TaskId(task.id), newEndDate: LocalDate(formatDateForInput(value)) })
			} else {
				onUpdateTask({ type: TaskCommandType.ChangeEndDate, taskId: TaskId(task.id), newEndDate: LocalDate(formatDateForInput(value)) })
			}
		}
	}

	const handleStatusChange = (newStatus: ProjectTaskStatus) => {
		setShowStatusMenu(false)
		if (newStatus !== task.status) {
			onUpdateTask({ type: TaskCommandType.ChangeStatus, taskId: TaskId(task.id), newStatus })
		}
	}

	return (
		<div
			ref={(node) => {
				elementRef.current = node
				dragRef(node)
			}}
			className={`group flex items-center gap-2 px-3 border-b border-gray-200 dark:border-zinc-800 ${
				isDragging ? 'opacity-50' : ''
			} ${isPoolTask ? 'cursor-grab active:cursor-grabbing' : ''}`}
			style={{ height: rowHeight }}
		>
			{/* E1: Status dot with dropdown */}
			<div className="relative shrink-0">
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

			<div className="flex-1 min-w-0">
				{isEditing ? (
					<div className="flex flex-col gap-0.5">
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
							className="w-full text-sm bg-transparent border-b border-blue-500 outline-none text-gray-900 dark:text-white py-0.5"
							autoFocus
						/>
						{/* E2: Description inline editing */}
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
							className="w-full text-xs bg-transparent border-b border-gray-300 dark:border-zinc-600 outline-none text-gray-500 dark:text-zinc-400 py-0.5"
						/>
					</div>
				) : (
					<div
						className="cursor-pointer"
						onClick={() => {
							setEditTitle(task.title)
							setEditDescription(task.description)
							setIsEditing(true)
						}}
					>
						<span
							className="text-sm text-gray-900 dark:text-white truncate block hover:text-blue-600 dark:hover:text-blue-400"
							title={task.title}
						>
							{task.title || 'Без названия'}
						</span>
						{task.description && (
							<span className="text-xs text-gray-400 dark:text-zinc-500 truncate block">
								{task.description}
							</span>
						)}
					</div>
				)}
			</div>

			<div className="relative shrink-0">
				<span
					onClick={() => startCalRef.current?.show()}
					className="text-xs text-gray-500 dark:text-zinc-400 cursor-pointer hover:text-blue-600 dark:hover:text-blue-400 tabular-nums w-18 text-center inline-block"
				>
					{formatDate(task.start ?? undefined)}
				</span>
				<Calendar
					ref={startCalRef}
					value={task.start ?? null}
					onChange={(e) => handleStartDateChange(e.value as Date | null)}
					dateFormat="dd.mm.yy"
					locale="ru"
					inputClassName="!w-0 !h-0 !p-0 !border-0 !opacity-0 !absolute"
					panelClassName="gantt-date-panel"
				/>
			</div>

			<div className="relative shrink-0">
				<span
					onClick={() => endCalRef.current?.show()}
					className="text-xs text-gray-500 dark:text-zinc-400 cursor-pointer hover:text-blue-600 dark:hover:text-blue-400 tabular-nums w-18 text-center inline-block"
				>
					{formatDate(task.end ?? undefined)}
				</span>
				<Calendar
					ref={endCalRef}
					value={task.end ?? null}
					onChange={(e) => handleEndDateChange(e.value)}
					dateFormat="dd.mm.yy"
					locale="ru"
					inputClassName="!w-0 !h-0 !p-0 !border-0 !opacity-0 !absolute"
					panelClassName="gantt-date-panel"
				/>
			</div>

			<button
				onClick={() => onUpdateTask({ type: TaskCommandType.DeleteTask, taskId: TaskId(task.id) })}
				className="opacity-0 group-hover:opacity-100 shrink-0 text-gray-400 hover:text-red-500 transition-opacity"
			>
				<Trash2 size={14} />
			</button>
		</div>
	)
}
