'use client'

import { useState } from 'react'
import { GanttTask } from '@/types'
import { getDayOffset } from '@/utils/ganttDateUtils'
import { ganttBar } from './GanttChart.styles'

interface AssignmentIndicator {
	resourceName: string
	resourceId: string
}

interface GanttBarProps {
	task: GanttTask
	rangeStart: Date
	dayWidth: number
	previewOffsetPx?: number
	previewWidthPx?: number
	isCritical?: boolean
	slack?: number
	assignments?: AssignmentIndicator[]
	onLinkStart?: (taskId: string, side: 'start' | 'end', e: React.MouseEvent) => void
	onDragStart?: (taskId: string, e: React.MouseEvent) => void
	onResizeStartLeft?: (taskId: string, e: React.MouseEvent) => void
	onResizeStart?: (taskId: string, e: React.MouseEvent) => void
	onAssignmentClick?: (taskId: string, e: React.MouseEvent) => void
}

const fmtDate = (d?: Date | null) => {
	if (!d) return '—'
	return d.toLocaleDateString('ru-RU', { day: '2-digit', month: '2-digit', year: 'numeric' })
}

export default function GanttBar({
	task,
	rangeStart,
	dayWidth,
	previewOffsetPx = 0,
	previewWidthPx,
	isCritical,
	slack,
	assignments,
	onLinkStart,
	onDragStart,
	onResizeStartLeft,
	onResizeStart,
	onAssignmentClick,
}: GanttBarProps) {
	const [hover, setHover] = useState<{ x: number; y: number } | null>(null)

	if (!task.start || !task.end) return null

	const startOffset = getDayOffset(task.start, rangeStart)
	const endOffset = getDayOffset(task.end, rangeStart)
	const left = startOffset * dayWidth
	const durationDays = endOffset - startOffset + 1
	const baseWidth = durationDays * dayWidth
	const width = previewWidthPx ?? baseWidth

	const totalEffort = (task.baselineEffortHours ?? 0) + (task.additionalEffortHours ?? 0)

	return (
		<div
			className={`${ganttBar({ status: task.status })}${isCritical ? ' ring-2 ring-red-500 ring-offset-1' : ''}`}
			style={{
				left,
				width,
				transform: previewOffsetPx !== 0 ? `translateX(${previewOffsetPx}px)` : undefined,
				cursor: onDragStart ? 'grab' : undefined,
			}}
			onMouseDown={(e) => {
				if ((e.target as HTMLElement).dataset.handle) return
				onDragStart?.(task.id, e)
			}}
			onMouseEnter={(e) => setHover({ x: e.clientX, y: e.clientY })}
			onMouseMove={(e) => setHover({ x: e.clientX, y: e.clientY })}
			onMouseLeave={() => setHover(null)}
		>
			{/* Compact bar content */}
			<span className="text-white text-xs truncate px-2 font-medium flex-1">
				{task.title}
			</span>
			{/* Duration indicator */}
			<span className="text-white/70 text-[10px] pr-1.5 shrink-0 tabular-nums">
				{durationDays}д
			</span>
			{/* Effort coverage badge */}
			{task.effortCoveragePercent != null && (
				<span
					className={`text-[9px] px-1 rounded-sm font-medium shrink-0 ${
						task.effortCoveragePercent >= 100
							? 'bg-emerald-500/30 text-emerald-100'
							: task.effortCoveragePercent >= 50
								? 'bg-amber-400/30 text-amber-100'
								: 'bg-red-400/30 text-red-100'
					}`}
				>
					{Math.round(task.effortCoveragePercent)}%
				</span>
			)}
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
			{/* Link handles */}
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
			{/* Rich tooltip */}
			{hover && (
				<div
					className="fixed z-50 pointer-events-none"
					style={{ left: hover.x, top: hover.y - 8, transform: 'translate(-50%, -100%)' }}
				>
					<div className="bg-gray-900 text-white text-[10px] rounded px-2.5 py-1.5 shadow-lg space-y-0.5 max-w-72 whitespace-nowrap">
						<div className="font-medium text-[11px] truncate max-w-64">{task.title}</div>
						<div className="flex gap-3 text-white/70">
							<span>Начало: {fmtDate(task.start)}</span>
							<span>Окончание: {fmtDate(task.end)}</span>
						</div>
						<div className="text-white/70">
							Длительность: {durationDays} дн.
							{isCritical && <span className="ml-2 text-red-300 font-medium">Критический путь</span>}
							{!isCritical && slack !== undefined && <span className="ml-2">Запас: {slack} дн.</span>}
						</div>
						{/* Baseline dates */}
						{(task.baselineStart || task.baselineEnd) && (
							<div className="text-white/50">
								Базовый план: {fmtDate(task.baselineStart)} — {fmtDate(task.baselineEnd)}
							</div>
						)}
						{/* Actual dates */}
						{(task.actualStart || task.actualEnd) && (
							<div className="text-emerald-300">
								Факт: {fmtDate(task.actualStart)} — {fmtDate(task.actualEnd)}
							</div>
						)}
						{/* Assignments */}
						{assignments && assignments.length > 0 && (
							<div className="text-white/70">
								Ресурсы: {assignments.map((a) => a.resourceName).join(', ')}
							</div>
						)}
						{/* Effort */}
						{totalEffort > 0 && (
							<div className={`border-t border-white/20 pt-0.5 ${
								(task.effortCoveragePercent ?? 0) >= 100
									? 'text-emerald-300'
									: (task.effortCoveragePercent ?? 0) >= 50
										? 'text-amber-300'
										: 'text-red-300'
							}`}>
								Трудоёмкость: {task.allocatedEffortHours ?? 0}ч / {totalEffort}ч
								{task.effortCoveragePercent != null && ` (${Math.round(task.effortCoveragePercent)}%)`}
							</div>
						)}
					</div>
				</div>
			)}
		</div>
	)
}
