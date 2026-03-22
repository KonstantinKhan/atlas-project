'use client'

import { useState } from 'react'
import { Plus } from 'lucide-react'
import { GanttTask, TaskCommand } from '@/types'
import { TaskCommandType } from '@/types/types/TaskCommandType'
import GanttTaskRow from './GanttTaskRow'
import { useSortable } from '@dnd-kit/react/sortable'

interface GanttTaskListProps {
	tasks: GanttTask[]
	headerHeight: number
	rowHeight: number
	assignmentsByTask?: Map<string, Array<{ resourceName: string; resourceId: string }>>
	onUpdateTask: (cmd: TaskCommand) => void
	onReorder: (orderedIds: string[]) => void
	onAssignmentClick?: (taskId: string, e: React.MouseEvent) => void
}

function SortableTaskRow({
	task,
	index,
	rowHeight,
	assignments,
	onUpdateTask,
	onAssignmentClick,
}: {
	task: GanttTask
	index: number
	rowHeight: number
	assignments?: Array<{ resourceName: string; resourceId: string }>
	onUpdateTask: (cmd: TaskCommand) => void
	onAssignmentClick?: (taskId: string, e: React.MouseEvent) => void
}) {
	const { ref, handleRef, isDragging } = useSortable({
		id: task.id,
		index,
		group: 'task-list',
		data: { type: 'reorder', taskId: task.id },
	})

	return (
		<div ref={ref} style={{ opacity: isDragging ? 0.5 : 1 }}>
			<GanttTaskRow
				task={task}
				rowHeight={rowHeight}
				assignments={assignments}
				onUpdateTask={onUpdateTask}
				onAssignmentClick={onAssignmentClick}
				dragHandleRef={handleRef}
			/>
		</div>
	)
}

export default function GanttTaskList({
	tasks,
	headerHeight,
	rowHeight,
	assignmentsByTask,
	onUpdateTask,
	onReorder,
	onAssignmentClick,
}: GanttTaskListProps) {
	const [isCreating, setIsCreating] = useState(false)
	const [newTaskTitle, setNewTaskTitle] = useState('')

	const handleStartCreate = () => {
		setIsCreating(true)
		setNewTaskTitle('')
	}

	const handleCancelCreate = () => {
		setIsCreating(false)
		setNewTaskTitle('')
	}

	const handleSubmitCreate = () => {
		const title = newTaskTitle.trim()
		if (title) {
			onUpdateTask({ type: TaskCommandType.CreateTaskInPool, title })
			setIsCreating(false)
			setNewTaskTitle('')
		}
	}

	const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
		if (e.key === 'Enter') {
			handleSubmitCreate()
		} else if (e.key === 'Escape') {
			handleCancelCreate()
		}
	}

	return (
		<div>
			<div
				className="flex items-end px-3 pb-1.5 border-b border-gray-300 dark:border-zinc-700"
				style={{ height: headerHeight }}
			>
				<div className="flex items-center gap-2 w-full text-xs font-semibold text-gray-500 dark:text-zinc-500 uppercase tracking-wider">
					<span className="flex-1">Задача</span>
				</div>
			</div>

			{tasks.map((task, index) => (
				<SortableTaskRow
					key={task.id}
					task={task}
					index={index}
					rowHeight={rowHeight}
					assignments={assignmentsByTask?.get(task.id)}
					onUpdateTask={onUpdateTask}
					onAssignmentClick={onAssignmentClick}
				/>
			))}

			{isCreating ? (
				<div
					className="flex items-center gap-2 px-3 py-2.5 border-b border-gray-200 dark:border-zinc-800 bg-blue-50 dark:bg-blue-900/20"
					style={{ height: rowHeight }}
				>
					<div className="w-2.5 h-2.5 rounded-full shrink-0 bg-gray-300 dark:bg-zinc-600" />
					<input
						type="text"
						value={newTaskTitle}
						onChange={(e) => setNewTaskTitle(e.target.value)}
						onKeyDown={handleKeyDown}
						onBlur={handleCancelCreate}
						placeholder="Введите название задачи..."
						className="flex-1 text-sm bg-transparent border-b border-blue-500 outline-none text-gray-900 dark:text-white py-0.5"
						autoFocus
					/>
				</div>
			) : (
				<button
					onClick={handleStartCreate}
					className="flex items-center gap-2 w-full px-3 py-2.5 text-sm text-gray-500 dark:text-zinc-500 hover:text-blue-600 dark:hover:text-blue-400 hover:bg-gray-50 dark:hover:bg-zinc-900 transition-colors border-b border-gray-200 dark:border-zinc-800"
				>
					<Plus size={16} />
					Добавить задачу
				</button>
			)}
		</div>
	)
}
