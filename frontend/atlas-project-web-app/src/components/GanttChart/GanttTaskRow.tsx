'use client'

import { useState, useRef } from 'react'
import { Task, TaskCommand } from '@/types'
import { TaskCommandType } from '@/types/types/TaskCommandType'
import { TaskId } from '@/utils/types/TaskId'
import { LocalDate } from '@/utils/types/LocalDate'
import { formatDateForInput } from '@/utils/ganttDateUtils'
import { Calendar } from 'primereact/calendar'
import { addLocale } from 'primereact/api'

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
	task: Task
	rowHeight: number
	onUpdateTask: (cmd: TaskCommand) => void
}

const statusColors: Record<string, string> = {
	empty: 'bg-gray-300 dark:bg-zinc-600',
	backlog: 'bg-slate-400 dark:bg-slate-600',
	'in progress': 'bg-blue-500 dark:bg-blue-600',
	done: 'bg-emerald-500 dark:bg-emerald-600',
	blocked: 'bg-red-400 dark:bg-red-600',
}

export default function GanttTaskRow({
	task,
	rowHeight,
	onUpdateTask,
}: GanttTaskRowProps) {
	const [isEditing, setIsEditing] = useState(false)
	const [editTitle, setEditTitle] = useState(task.title)
	const startCalRef = useRef<Calendar>(null)
	const endCalRef = useRef<Calendar>(null)

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
	}

	const handleStartDateChange = (value: Date | null | undefined) => {
		if (value !== null && value !== undefined) {
			onUpdateTask({ type: TaskCommandType.ChangeStartDate, taskId: TaskId(task.id), newStartDate: LocalDate(formatDateForInput(value)) })
		}
	}

	const handleEndDateChange = (value: Date | null | undefined) => {
		if (value !== null && value !== undefined) {
			onUpdateTask({ type: TaskCommandType.ChangeEndDate, taskId: TaskId(task.id), newEndDate: LocalDate(formatDateForInput(value)) })
		}
	}

	return (
		<div
			className="flex items-center gap-2 px-3 border-b border-gray-200 dark:border-zinc-800"
			style={{ height: rowHeight }}
		>
			<div
				className={`w-2.5 h-2.5 rounded-full shrink-0 ${statusColors[task.status] || statusColors.empty}`}
			/>

			<div className="flex-1 min-w-0">
				{isEditing ? (
					<input
						type="text"
						value={editTitle}
						onChange={(e) => setEditTitle(e.target.value)}
						onBlur={handleTitleSubmit}
						onKeyDown={(e) => {
							if (e.key === 'Enter') handleTitleSubmit()
							if (e.key === 'Escape') {
								setEditTitle(task.title)
								setIsEditing(false)
							}
						}}
						className="w-full text-sm bg-transparent border-b border-blue-500 outline-none text-gray-900 dark:text-white py-0.5"
						autoFocus
					/>
				) : (
					<span
						className="text-sm text-gray-900 dark:text-white truncate block cursor-pointer hover:text-blue-600 dark:hover:text-blue-400"
						onClick={() => setIsEditing(true)}
						title={task.title}
					>
						{task.title || 'Без названия'}
					</span>
				)}
			</div>

			<div className="relative shrink-0">
				<span
					onClick={() => startCalRef.current?.show()}
					className="text-xs text-gray-500 dark:text-zinc-400 cursor-pointer hover:text-blue-600 dark:hover:text-blue-400 tabular-nums w-18 text-center inline-block"
				>
					{formatDate(task.plannedStartDate)}
				</span>
				<Calendar
					ref={startCalRef}
					value={task.plannedStartDate ?? null}
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
					{formatDate(task.plannedEndDate)}
				</span>
				<Calendar
					ref={endCalRef}
					value={task.plannedEndDate ?? null}
					onChange={(e) => handleEndDateChange(e.value)}
					dateFormat="dd.mm.yy"
					locale="ru"
					inputClassName="!w-0 !h-0 !p-0 !border-0 !opacity-0 !absolute"
					panelClassName="gantt-date-panel"
				/>
			</div>
		</div>
	)
}
