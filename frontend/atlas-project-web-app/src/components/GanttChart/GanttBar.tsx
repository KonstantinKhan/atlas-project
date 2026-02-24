'use client'

import { Task } from '@/types'
import { getDayOffset } from '@/utils/ganttDateUtils'
import { ganttBar } from './GanttChart.styles'

interface GanttBarProps {
	task: Task
	rangeStart: Date
	dayWidth: number
	onLinkStart?: (taskId: string, e: React.MouseEvent) => void
	isLinkTarget?: boolean
}

export default function GanttBar({
	task,
	rangeStart,
	dayWidth,
	onLinkStart,
	isLinkTarget,
}: GanttBarProps) {
	if (!task.plannedStartDate || !task.plannedEndDate) return null

	const startOffset = getDayOffset(task.plannedStartDate, rangeStart)
	const endOffset = getDayOffset(task.plannedEndDate, rangeStart)
	const left = startOffset * dayWidth
	const width = (endOffset - startOffset + 1) * dayWidth

	return (
		<div
			className={ganttBar({ status: task.status })}
			style={{ left, width }}
		>
			<span className="text-white text-xs truncate px-2 font-medium">
				{task.title}
			</span>
			{/* Right handle — shown only in normal mode */}
			{onLinkStart && !isLinkTarget && (
				<div
					className="absolute right-0 top-1/2 -translate-y-1/2 translate-x-1/2
					           w-3 h-3 rounded-full bg-white border-2 border-blue-400
					           cursor-crosshair opacity-0 group-hover:opacity-100 z-10
					           transition-opacity"
					onMouseDown={(e) => {
						e.stopPropagation()
						onLinkStart(task.id, e)
					}}
				/>
			)}
			{/* Left handle — shown only when this bar is a potential link target */}
			{isLinkTarget && (
				<div
					className="absolute left-0 top-1/2 -translate-y-1/2 -translate-x-1/2
					           w-3 h-3 rounded-full bg-white border-2 border-blue-400
					           opacity-0 group-hover:opacity-100 z-10
					           transition-opacity pointer-events-none"
				/>
			)}
		</div>
	)
}
