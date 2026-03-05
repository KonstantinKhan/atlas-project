'use client'

import { useState } from 'react'
import { GanttTask, GanttDependencyDto } from '@/types'
import { getDayOffset } from '@/utils/ganttDateUtils'
import GanttBar from './GanttBar'

interface DragState {
	taskId: string
	startX: number
	origStart: Date
}

interface ResizeState {
	taskId: string
	startX: number
	origEnd: Date
}

interface GanttTaskLayerProps {
	tasks: GanttTask[]
	dependencies: GanttDependencyDto[]
	days: Date[]
	rangeStart: Date
	dayWidth: number
	rowHeight: number
	onCreateDependency: (fromId: string, toId: string) => void
	onRemoveDependency: (fromId: string, toId: string) => void
	onMoveTask: (taskId: string, newStartDate: string) => void
	onResizeTask: (taskId: string, newEndDate: string) => void
	linkingFrom: string | null
	setLinkingFrom: (id: string | null) => void
	mousePos: { x: number; y: number }
	setMousePos: (pos: { x: number; y: number }) => void
}

function formatDate(date: Date): string {
	const y = date.getFullYear()
	const m = String(date.getMonth() + 1).padStart(2, '0')
	const d = String(date.getDate()).padStart(2, '0')
	return `${y}-${m}-${d}`
}

function addDays(date: Date, days: number): Date {
	const result = new Date(date)
	result.setDate(result.getDate() + days)
	return result
}

export default function GanttTaskLayer({
	tasks,
	dependencies,
	days,
	rangeStart,
	dayWidth,
	rowHeight,
	onCreateDependency,
	onRemoveDependency,
	onMoveTask,
	onResizeTask,
	linkingFrom,
	setLinkingFrom,
	mousePos,
	setMousePos,
}: GanttTaskLayerProps) {
	const [dragging, setDragging] = useState<DragState | null>(null)
	const [resizing, setResizing] = useState<ResizeState | null>(null)
	const [currentClientX, setCurrentClientX] = useState(0)

	const handleMouseMove = (e: React.MouseEvent) => {
		setCurrentClientX(e.clientX)
		if (linkingFrom) {
			const rect = (e.currentTarget as HTMLDivElement).getBoundingClientRect()
			setMousePos({ x: e.clientX - rect.left, y: e.clientY - rect.top })
		}
	}

	const handleMouseUp = (e: React.MouseEvent) => {
		if (linkingFrom) {
			const rect = (e.currentTarget as HTMLDivElement).getBoundingClientRect()
			const rowIndex = Math.floor((e.clientY - rect.top) / rowHeight)
			if (rowIndex >= 0 && rowIndex < tasks.length) {
				const target = tasks[rowIndex]
				if (target.id !== linkingFrom) {
					onCreateDependency(linkingFrom, target.id)
				}
			}
			setLinkingFrom(null)
			return
		}

		if (dragging) {
			const dayDelta = Math.round((e.clientX - dragging.startX) / dayWidth)
			const newStart = addDays(dragging.origStart, dayDelta)
			onMoveTask(dragging.taskId, formatDate(newStart))
			setDragging(null)
			return
		}

		if (resizing) {
			const dayDelta = Math.round((e.clientX - resizing.startX) / dayWidth)
			const task = tasks.find((t) => t.id === resizing.taskId)
			let newEnd = addDays(resizing.origEnd, dayDelta)
			if (task?.start && newEnd <= task.start) {
				newEnd = addDays(task.start, 1)
			}
			onResizeTask(resizing.taskId, formatDate(newEnd))
			setResizing(null)
		}
	}

	const isDraggingOrResizing = dragging !== null || resizing !== null

	return (
		<div
			className="relative"
			style={{
				cursor: linkingFrom
					? 'crosshair'
					: dragging
						? 'grabbing'
						: resizing
							? 'ew-resize'
							: undefined,
				userSelect: isDraggingOrResizing ? 'none' : undefined,
			}}
			onMouseMove={handleMouseMove}
			onMouseUp={handleMouseUp}
			onMouseLeave={() => {
				if (linkingFrom) setLinkingFrom(null)
				if (dragging) setDragging(null)
				if (resizing) setResizing(null)
			}}
		>
			{/* Task bars */}
			{tasks.map((task) => {
				let previewOffsetPx = 0
				let previewWidthPx: number | undefined = undefined

				if (dragging && dragging.taskId === task.id) {
					const dayDelta = Math.round((currentClientX - dragging.startX) / dayWidth)
					previewOffsetPx = dayDelta * dayWidth
				}

				if (resizing && resizing.taskId === task.id && task.start && task.end) {
					const dayDelta = Math.round((currentClientX - resizing.startX) / dayWidth)
					const startOffset = getDayOffset(task.start, rangeStart)
					const endOffset = getDayOffset(task.end, rangeStart)
					const newEndOffset = Math.max(startOffset, endOffset + dayDelta)
					previewWidthPx = (newEndOffset - startOffset + 1) * dayWidth
				}

				return (
					<div
						key={task.id}
						className="relative"
						style={{ height: rowHeight }}
					>
						<GanttBar
							task={task}
							rangeStart={rangeStart}
							dayWidth={dayWidth}
							previewOffsetPx={previewOffsetPx}
							previewWidthPx={previewWidthPx}
							onLinkStart={!linkingFrom && !isDraggingOrResizing ? (id, e) => {
								e.preventDefault()
								setLinkingFrom(id)
							} : undefined}
							isLinkTarget={linkingFrom !== null && linkingFrom !== task.id}
							onDragStart={!linkingFrom && !isDraggingOrResizing ? (id, e) => {
								if (!task.start) return
								setDragging({ taskId: id, startX: e.clientX, origStart: task.start })
								setCurrentClientX(e.clientX)
							} : undefined}
							onResizeStart={!linkingFrom && !isDraggingOrResizing ? (id, e) => {
								if (!task.end) return
								setResizing({ taskId: id, startX: e.clientX, origEnd: task.end })
								setCurrentClientX(e.clientX)
							} : undefined}
						/>
					</div>
				)
			})}

			{/* SVG overlay for dependency arrows */}
			<svg
				className="absolute inset-0 pointer-events-none"
				width={days.length * dayWidth}
				height={tasks.length * rowHeight}
				style={{ zIndex: 5 }}
			>
				<defs>
					<marker
						id="dep-arrow"
						markerWidth="6"
						markerHeight="6"
						refX="6"
						refY="3"
						orient="auto"
					>
						<path d="M0,0 L6,3 L0,6 Z" fill="#6366f1" />
					</marker>
				</defs>

				{/* Permanent dependency arrows */}
				{dependencies.flatMap((depRelation) => {
					const dep = tasks.find((t) => t.id === depRelation.toTaskId)
					const pred = tasks.find((t) => t.id === depRelation.fromTaskId)
					if (!pred?.end || !dep?.start) return []
					const predIdx = tasks.indexOf(pred)
					const depIdx = tasks.indexOf(dep)

					// Offset arrow endpoints to follow dragged/resized bars in real-time
					let x1Offset = 0
					let x2Offset = 0
					if (dragging) {
						const dragPx = Math.round((currentClientX - dragging.startX) / dayWidth) * dayWidth
						if (pred.id === dragging.taskId) x1Offset = dragPx
						if (dep.id === dragging.taskId) x2Offset = dragPx
					}
					if (resizing && resizing.taskId === pred.id && pred.start) {
						const dayDelta = Math.round((currentClientX - resizing.startX) / dayWidth)
						const endOffset = getDayOffset(pred.end, rangeStart)
						const startOffset = getDayOffset(pred.start, rangeStart)
						const newEndOffset = Math.max(startOffset, endOffset + dayDelta)
						x1Offset = (newEndOffset - endOffset) * dayWidth
					}

					const x1 = (getDayOffset(pred.end, rangeStart) + 1) * dayWidth + x1Offset
					const y1 = predIdx * rowHeight + rowHeight / 2
					const x2 = getDayOffset(dep.start, rangeStart) * dayWidth + x2Offset
					const y2 = depIdx * rowHeight + rowHeight / 2
					const ARM = 12
					const cornerX = x2 - ARM
					let d: string
					if (cornerX >= x1 + ARM) {
						d = `M${x1},${y1} L${cornerX},${y1} L${cornerX},${y2} L${x2},${y2}`
					} else {
						const wrapRight = x1 + ARM
						const wrapLeft = x2 - ARM
						const boundaryY = Math.min(predIdx, depIdx) * rowHeight + rowHeight
						d = `M${x1},${y1} L${wrapRight},${y1} L${wrapRight},${boundaryY} L${wrapLeft},${boundaryY} L${wrapLeft},${y2} L${x2},${y2}`
					}
					return [
						<path
							key={`${depRelation.fromTaskId}-${depRelation.toTaskId}`}
							d={d}
							fill="none"
							stroke="#6366f1"
							strokeWidth={1.5}
							strokeDasharray="4 2"
							markerEnd="url(#dep-arrow)"
							style={{ pointerEvents: 'stroke', cursor: 'pointer' }}
							onClick={() => onRemoveDependency(depRelation.fromTaskId, depRelation.toTaskId)}
						/>
					]
				})}

				{/* Temporary arrow while linking */}
				{linkingFrom &&
					(() => {
						const pred = tasks.find((t) => t.id === linkingFrom)
						if (!pred?.end) return null
						const predIdx = tasks.indexOf(pred)
						const x1 = (getDayOffset(pred.end, rangeStart) + 1) * dayWidth
						const y1 = predIdx * rowHeight + rowHeight / 2
						return (
							<line
								x1={x1}
								y1={y1}
								x2={mousePos.x}
								y2={mousePos.y}
								stroke="#6366f1"
								strokeWidth={1.5}
								strokeDasharray="4 2"
								opacity={0.6}
							/>
						)
					})()}
			</svg>
		</div>
	)
}
