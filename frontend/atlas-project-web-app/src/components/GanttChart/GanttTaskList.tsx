'use client'

import { Plus } from 'lucide-react'
import { Task } from '@/types'
import GanttTaskRow from './GanttTaskRow'

interface GanttTaskListProps {
	tasks: Task[]
	headerHeight: number
	rowHeight: number
	onAddTask: () => void
	onUpdateTask: (id: string, updates: Partial<Task>) => void
}

export default function GanttTaskList({
	tasks,
	headerHeight,
	rowHeight,
	onAddTask,
	onUpdateTask,
}: GanttTaskListProps) {
	return (
		<div>
			{/* Header matching calendar header height */}
			<div
				className="flex items-end px-3 pb-1.5 border-b border-gray-300 dark:border-zinc-700"
				style={{ height: headerHeight }}
			>
				<div className="flex items-center gap-2 w-full text-xs font-semibold text-gray-500 dark:text-zinc-500 uppercase tracking-wider">
					<span className="flex-1">Задача</span>
					<span className="w-[72px] shrink-0 text-center">Начало</span>
					<span className="w-[72px] shrink-0 text-center">Окончание</span>
				</div>
			</div>

			{/* Task rows */}
			{tasks.map((task) => (
				<GanttTaskRow
					key={task.id}
					task={task}
					rowHeight={rowHeight}
					onUpdateTask={onUpdateTask}
				/>
			))}

			{/* Add task button */}
			<button
				onClick={onAddTask}
				className="flex items-center gap-2 w-full px-3 py-2.5 text-sm text-gray-500 dark:text-zinc-500 hover:text-blue-600 dark:hover:text-blue-400 hover:bg-gray-50 dark:hover:bg-zinc-900 transition-colors border-b border-gray-200 dark:border-zinc-800"
			>
				<Plus size={16} />
				Добавить задачу
			</button>
		</div>
	)
}
