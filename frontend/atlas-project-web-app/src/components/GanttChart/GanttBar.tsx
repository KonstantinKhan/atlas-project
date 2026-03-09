'use client'

import { GanttTask } from '@/types'
import { getDayOffset } from '@/utils/ganttDateUtils'
import { ganttBar } from './GanttChart.styles'

interface GanttBarProps {
	task: GanttTask
	rangeStart: Date
	dayWidth: number
	previewOffsetPx?: number
	previewWidthPx?: number
	isCritical?: boolean
	slack?: number
	onLinkStart?: (taskId: string, side: 'start' | 'end', e: React.MouseEvent) => void
	onDragStart?: (taskId: string, e: React.MouseEvent) => void
	onResizeStartLeft?: (taskId: string, e: React.MouseEvent) => void
	onResizeStart?: (taskId: string, e: React.MouseEvent) => void
}

export default function GanttBar({
	task,
	rangeStart,
	dayWidth,
	previewOffsetPx = 0,
	previewWidthPx,
	isCritical,
	slack,
	onLinkStart,
	onDragStart,
	onResizeStartLeft,
	onResizeStart,
}: GanttBarProps) {
	if (!task.start || !task.end) return null

	const startOffset = getDayOffset(task.start, rangeStart)
	const endOffset = getDayOffset(task.end, rangeStart)
	const left = startOffset * dayWidth
	const durationDays = endOffset - startOffset + 1
	const baseWidth = durationDays * dayWidth
	const width = previewWidthPx ?? baseWidth

	return (
		<div
			className={`${ganttBar({ status: task.status })}${isCritical ? ' ring-2 ring-red-500 ring-offset-1' : ''}`}
			title={isCritical ? 'Критическая задача' : slack !== undefined ? `Запас: ${slack} дн.` : undefined}
			style={{
				left,
				width,
				transform: previewOffsetPx !== 0 ? `translateX(${previewOffsetPx}px)` : undefined,
				cursor: onDragStart ? 'grab' : undefined,
			}}
			onMouseDown={(e) => {
				// Don't start drag if clicking on handles
				if ((e.target as HTMLElement).dataset.handle) return
				onDragStart?.(task.id, e)
			}}
		>
			<span className="text-white text-xs truncate px-2 font-medium flex-1">
				{task.title}
			</span>
			{/* E4: Duration indicator */}
			<span className="text-white/70 text-[10px] pr-1.5 shrink-0 tabular-nums">
				{durationDays}д
			</span>
			{/* Resize handle — left edge */}
			{onResizeStartLeft && (
				<div
					data-handle="resize"
					className="absolute left-0 top-0 w-2 h-full cursor-ew-resize z-10"
					onMouseDown={(e) => {
						e.stopPropagation()
						onResizeStartLeft(task.id, e)
					}}
				/>
			)}
			{/* Resize handle — right edge */}
			{onResizeStart && (
				<div
					data-handle="resize"
					className="absolute right-0 top-0 w-2 h-full cursor-ew-resize z-10"
					onMouseDown={(e) => {
						e.stopPropagation()
						onResizeStart(task.id, e)
					}}
				/>
			)}
			{/* Link handles — left (start) and right (end) */}
			{onLinkStart && (
				<>
					<div
						data-handle="link"
						className="absolute left-0 top-1/2 -translate-y-1/2 -translate-x-1/2
						           w-3 h-3 rounded-full bg-white border-2 border-blue-400
						           cursor-crosshair opacity-0 group-hover:opacity-100 z-10
						           transition-opacity"
						onMouseDown={(e) => {
							e.stopPropagation()
							onLinkStart(task.id, 'start', e)
						}}
					/>
					<div
						data-handle="link"
						className="absolute right-0 top-1/2 -translate-y-1/2 translate-x-1/2
						           w-3 h-3 rounded-full bg-white border-2 border-blue-400
						           cursor-crosshair opacity-0 group-hover:opacity-100 z-10
						           transition-opacity"
						onMouseDown={(e) => {
							e.stopPropagation()
							onLinkStart(task.id, 'end', e)
						}}
					/>
				</>
			)}
		</div>
	)
}
