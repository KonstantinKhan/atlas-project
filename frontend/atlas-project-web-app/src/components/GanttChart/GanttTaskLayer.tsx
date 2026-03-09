'use client'

import { useState, useRef } from 'react'
import { GanttTask, GanttDependencyDto } from '@/types'
import { getDayOffset } from '@/utils/ganttDateUtils'
import GanttBar from './GanttBar'
import DependencyActionPopover from './DependencyActionPopover'
import type { LinkSide } from './GanttCalendarGrid'
import type { ViewMode } from '@/store/timelineCalendarStore'

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

interface ResizeLeftState {
	taskId: string
	startX: number
	origStart: Date
}

interface LinkingFrom {
	taskId: string
	side: LinkSide
}

interface LinkTarget {
	rowIdx: number
	x: number
	y: number
}

interface GanttTaskLayerProps {
	tasks: GanttTask[]
	dependencies: GanttDependencyDto[]
	days: Date[]
	rangeStart: Date
	dayWidth: number
	rowHeight: number
	viewMode?: ViewMode
	criticalTaskIds?: Set<string>
	slackMap?: Map<string, number>
	onCreateDependency: (fromId: string, toId: string, type: string) => void
	onChangeDependencyType: (fromId: string, toId: string, newType: string) => void
	onRemoveDependency: (fromId: string, toId: string) => void
	onMoveTask: (taskId: string, newStartDate: string) => void
	onResizeTask: (taskId: string, newEndDate: string) => void
	onResizeFromStart: (taskId: string, newStartDate: string) => void
	linkingFrom: LinkingFrom | null
	setLinkingFrom: (v: LinkingFrom | null) => void
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

function getDependencyType(fromSide: LinkSide, toSide: LinkSide): string {
	if (fromSide === 'end' && toSide === 'start') return 'FS'
	if (fromSide === 'start' && toSide === 'start') return 'SS'
	if (fromSide === 'end' && toSide === 'end') return 'FF'
	return 'SF'
}

export default function GanttTaskLayer({
	tasks,
	dependencies,
	days,
	rangeStart,
	dayWidth,
	rowHeight,
	viewMode = 'day',
	criticalTaskIds,
	slackMap,
	onCreateDependency,
	onChangeDependencyType,
	onRemoveDependency,
	onMoveTask,
	onResizeTask,
	onResizeFromStart,
	linkingFrom,
	setLinkingFrom,
	mousePos,
	setMousePos,
}: GanttTaskLayerProps) {
	const [dragging, setDragging] = useState<DragState | null>(null)
	const [resizing, setResizing] = useState<ResizeState | null>(null)
	const [resizingLeft, setResizingLeft] = useState<ResizeLeftState | null>(null)
	const [currentClientX, setCurrentClientX] = useState(0)
	const [hoveredDep, setHoveredDep] = useState<string | null>(null)
	const [clickedDep, setClickedDep] = useState<{
		fromId: string; toId: string
		position: { x: number; y: number }
	} | null>(null)
	const [linkTarget, setLinkTarget] = useState<LinkTarget | null>(null)
	const prevTargetRowRef = useRef<number | null>(null)
	const skipNextMouseUp = useRef(false)

	const handleMouseMove = (e: React.MouseEvent) => {
		setCurrentClientX(e.clientX)
		if (linkingFrom) {
			const rect = (e.currentTarget as HTMLDivElement).getBoundingClientRect()
			const relX = e.clientX - rect.left
			const relY = e.clientY - rect.top
			setMousePos({ x: relX, y: relY })

			const rowIdx = Math.floor(relY / rowHeight)
			const isValidTarget = rowIdx >= 0 && rowIdx < tasks.length
				&& tasks[rowIdx].id !== linkingFrom.taskId
				&& tasks[rowIdx].start && tasks[rowIdx].end

			if (isValidTarget && rowIdx !== prevTargetRowRef.current) {
				prevTargetRowRef.current = rowIdx
				setLinkTarget({ rowIdx, x: relX, y: rowIdx * rowHeight + rowHeight / 2 })
			} else if (!isValidTarget) {
				prevTargetRowRef.current = null
				setLinkTarget(null)
			}
		}
	}

	const snapDays = viewMode === 'week' ? 7 : 1
	const snapPx = dayWidth * snapDays

	const handleMouseUp = (e: React.MouseEvent) => {
		if (linkingFrom) {
			if (skipNextMouseUp.current) {
				skipNextMouseUp.current = false
				return
			}
			setLinkingFrom(null)
			setLinkTarget(null)
			prevTargetRowRef.current = null
			return
		}

		if (dragging) {
			const dayDelta = Math.round((e.clientX - dragging.startX) / snapPx) * snapDays
			const newStart = addDays(dragging.origStart, dayDelta)
			onMoveTask(dragging.taskId, formatDate(newStart))
			setDragging(null)
			return
		}

		if (resizing) {
			const dayDelta = Math.round((e.clientX - resizing.startX) / snapPx) * snapDays
			const task = tasks.find((t) => t.id === resizing.taskId)
			let newEnd = addDays(resizing.origEnd, dayDelta)
			if (task?.start && newEnd <= task.start) {
				newEnd = addDays(task.start, 1)
			}
			onResizeTask(resizing.taskId, formatDate(newEnd))
			setResizing(null)
			return
		}

		if (resizingLeft) {
			const dayDelta = Math.round((e.clientX - resizingLeft.startX) / snapPx) * snapDays
			const task = tasks.find((t) => t.id === resizingLeft.taskId)
			let newStart = addDays(resizingLeft.origStart, dayDelta)
			if (task?.end && newStart >= task.end) {
				newStart = addDays(task.end, -1)
			}
			onResizeFromStart(resizingLeft.taskId, formatDate(newStart))
			setResizingLeft(null)
		}
	}

	const handleTargetSideClick = (toSide: LinkSide) => {
		if (!linkingFrom || !linkTarget) return
		const type = getDependencyType(linkingFrom.side, toSide)
		onCreateDependency(linkingFrom.taskId, tasks[linkTarget.rowIdx].id, type)
		setLinkingFrom(null)
		setLinkTarget(null)
		prevTargetRowRef.current = null
	}

	const isDraggingOrResizing = dragging !== null || resizing !== null || resizingLeft !== null

	return (
		<div
			className="relative"
			style={{
				cursor: linkingFrom
					? 'crosshair'
					: dragging
						? 'grabbing'
						: (resizing || resizingLeft)
							? 'ew-resize'
							: undefined,
				userSelect: isDraggingOrResizing ? 'none' : undefined,
			}}
			onMouseMove={handleMouseMove}
			onMouseUp={handleMouseUp}
			onMouseLeave={() => {
				if (linkingFrom) {
					setLinkingFrom(null)
					setLinkTarget(null)
					prevTargetRowRef.current = null
				}
				if (dragging) setDragging(null)
				if (resizing) setResizing(null)
				if (resizingLeft) setResizingLeft(null)
				setClickedDep(null)
			}}
		>
			{/* Task bars */}
			{tasks.map((task) => {
				let previewOffsetPx = 0
				let previewWidthPx: number | undefined = undefined

				if (dragging && dragging.taskId === task.id) {
					const dayDelta = Math.round((currentClientX - dragging.startX) / snapPx) * snapDays
					previewOffsetPx = dayDelta * dayWidth
				}

				if (resizing && resizing.taskId === task.id && task.start && task.end) {
					const dayDelta = Math.round((currentClientX - resizing.startX) / snapPx) * snapDays
					const startOffset = getDayOffset(task.start, rangeStart)
					const endOffset = getDayOffset(task.end, rangeStart)
					const newEndOffset = Math.max(startOffset, endOffset + dayDelta)
					previewWidthPx = (newEndOffset - startOffset + 1) * dayWidth
				}

				if (resizingLeft && resizingLeft.taskId === task.id && task.start && task.end) {
					const dayDelta = Math.round((currentClientX - resizingLeft.startX) / snapPx) * snapDays
					const startOffset = getDayOffset(task.start, rangeStart)
					const endOffset = getDayOffset(task.end, rangeStart)
					const newStartOffset = Math.min(endOffset, startOffset + dayDelta)
					previewOffsetPx = (newStartOffset - startOffset) * dayWidth
					previewWidthPx = (endOffset - newStartOffset + 1) * dayWidth
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
							isCritical={criticalTaskIds?.has(task.id)}
							slack={slackMap?.get(task.id)}
							onLinkStart={!linkingFrom && !isDraggingOrResizing ? (id, side, e) => {
								e.preventDefault()
								setLinkingFrom({ taskId: id, side })
								setClickedDep(null)
								skipNextMouseUp.current = true
							} : undefined}
							onDragStart={!linkingFrom && !isDraggingOrResizing ? (id, e) => {
								if (!task.start) return
								setDragging({ taskId: id, startX: e.clientX, origStart: task.start })
								setCurrentClientX(e.clientX)
								setClickedDep(null)
							} : undefined}
							onResizeStartLeft={!linkingFrom && !isDraggingOrResizing ? (id, e) => {
								if (!task.start) return
								setResizingLeft({ taskId: id, startX: e.clientX, origStart: task.start })
								setCurrentClientX(e.clientX)
								setClickedDep(null)
							} : undefined}
							onResizeStart={!linkingFrom && !isDraggingOrResizing ? (id, e) => {
								if (!task.end) return
								setResizing({ taskId: id, startX: e.clientX, origEnd: task.end })
								setCurrentClientX(e.clientX)
								setClickedDep(null)
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
					<filter id="dep-glow" x="-50%" y="-50%" width="200%" height="200%">
						<feDropShadow dx="0" dy="0" stdDeviation="4" floodColor="#a5b4fc" floodOpacity="1" />
					</filter>
					<marker
						id="dep-arrow-critical"
						markerWidth="6"
						markerHeight="6"
						refX="6"
						refY="3"
						orient="auto"
					>
						<path d="M0,0 L6,3 L0,6 Z" fill="#ef4444" />
					</marker>
					<filter id="dep-glow-critical" x="-50%" y="-50%" width="200%" height="200%">
						<feDropShadow dx="0" dy="0" stdDeviation="4" floodColor="#fca5a5" floodOpacity="1" />
					</filter>
				</defs>

				{/* Permanent dependency arrows */}
				{dependencies.flatMap((depRelation) => {
					const succ = tasks.find((t) => t.id === depRelation.toTaskId)
					const pred = tasks.find((t) => t.id === depRelation.fromTaskId)
					if (!pred?.start || !pred?.end || !succ?.start || !succ?.end) return []
					const predIdx = tasks.indexOf(pred)
					const succIdx = tasks.indexOf(succ)
					const depType = depRelation.type || 'FS'

					// Drag/resize offsets
					let dragPx = 0
					if (dragging) {
						dragPx = Math.round((currentClientX - dragging.startX) / snapPx) * snapDays * dayWidth
					}
					let resizeDeltaPx = 0
					if (resizing && pred.start) {
						const dayDelta = Math.round((currentClientX - resizing.startX) / snapPx) * snapDays
						const endOff = getDayOffset(pred.end, rangeStart)
						const startOff = getDayOffset(pred.start, rangeStart)
						const newEndOff = Math.max(startOff, endOff + dayDelta)
						resizeDeltaPx = (newEndOff - endOff) * dayWidth
					}
					let resizeLeftDeltaPx = 0
					if (resizingLeft && pred.start && pred.end) {
						const dayDelta = Math.round((currentClientX - resizingLeft.startX) / snapPx) * snapDays
						const startOff = getDayOffset(pred.start, rangeStart)
						const endOff = getDayOffset(pred.end, rangeStart)
						const newStartOff = Math.min(endOff, startOff + dayDelta)
						resizeLeftDeltaPx = (newStartOff - startOff) * dayWidth
					}

					// Anchor points based on dependency type
					const predStartX = getDayOffset(pred.start, rangeStart) * dayWidth
					const predEndX = (getDayOffset(pred.end, rangeStart) + 1) * dayWidth
					const succStartX = getDayOffset(succ.start, rangeStart) * dayWidth
					const succEndX = (getDayOffset(succ.end, rangeStart) + 1) * dayWidth

					let x1: number, x2: number
					let fromEnd: boolean, toStart: boolean

					switch (depType) {
						case 'SS':
							fromEnd = false; toStart = true
							x1 = predStartX
							x2 = succStartX
							break
						case 'FF':
							fromEnd = true; toStart = false
							x1 = predEndX
							x2 = succEndX
							break
						case 'SF':
							fromEnd = false; toStart = false
							x1 = predStartX
							x2 = succEndX
							break
						default: // FS
							fromEnd = true; toStart = true
							x1 = predEndX
							x2 = succStartX
							break
					}

					// Apply drag offsets
					if (dragging) {
						if (pred.id === dragging.taskId) x1 += dragPx
						if (succ.id === dragging.taskId) x2 += dragPx
					}
					if (resizing && resizing.taskId === pred.id && fromEnd) {
						x1 += resizeDeltaPx
					}
					if (resizing && resizing.taskId === succ.id && !toStart) {
						x2 += resizeDeltaPx
					}
					// Left resize offsets (affects start-side anchors)
					if (resizingLeft && resizingLeft.taskId === pred.id && !fromEnd) {
						x1 += resizeLeftDeltaPx
					}
					if (resizingLeft && resizingLeft.taskId === succ.id && toStart) {
						x2 += resizeLeftDeltaPx
					}

					const y1 = predIdx * rowHeight + rowHeight / 2
					const y2 = succIdx * rowHeight + rowHeight / 2
					const ARM = 12

					let d: string
					if (fromEnd && toStart) {
						// FS: right → left
						const cornerX = x2 - ARM
						if (cornerX >= x1 + ARM) {
							d = `M${x1},${y1} L${cornerX},${y1} L${cornerX},${y2} L${x2},${y2}`
						} else {
							const wrapRight = x1 + ARM
							const wrapLeft = x2 - ARM
							const boundaryY = Math.min(predIdx, succIdx) * rowHeight + rowHeight
							d = `M${x1},${y1} L${wrapRight},${y1} L${wrapRight},${boundaryY} L${wrapLeft},${boundaryY} L${wrapLeft},${y2} L${x2},${y2}`
						}
					} else if (!fromEnd && toStart) {
						// SS: left → left
						const minX = Math.min(x1, x2) - ARM
						d = `M${x1},${y1} L${minX},${y1} L${minX},${y2} L${x2},${y2}`
					} else if (fromEnd && !toStart) {
						// FF: right → right
						const maxX = Math.max(x1, x2) + ARM
						d = `M${x1},${y1} L${maxX},${y1} L${maxX},${y2} L${x2},${y2}`
					} else {
						// SF: left → right
						const cornerX = x2 + ARM
						if (x1 - ARM >= cornerX) {
							d = `M${x1},${y1} L${x1 - ARM},${y1} L${x1 - ARM},${y2} L${x2},${y2}`
						} else {
							const wrapLeft = x1 - ARM
							const wrapRight = x2 + ARM
							const boundaryY = Math.min(predIdx, succIdx) * rowHeight + rowHeight
							d = `M${x1},${y1} L${wrapLeft},${y1} L${wrapLeft},${boundaryY} L${wrapRight},${boundaryY} L${wrapRight},${y2} L${x2},${y2}`
						}
					}

					const strokeStyle = depType === 'FS' ? 'none'
						: depType === 'SF' ? '2 3'
						: '5 3'

					const depKey = `${depRelation.fromTaskId}-${depRelation.toTaskId}`
					const isHovered = hoveredDep === depKey
					const isCriticalArrow = criticalTaskIds?.has(depRelation.fromTaskId) && criticalTaskIds?.has(depRelation.toTaskId)
					const arrowColor = isCriticalArrow ? '#ef4444' : '#6366f1'
					const arrowHoverColor = isCriticalArrow ? '#f87171' : '#818cf8'
					const arrowMarker = isCriticalArrow ? 'url(#dep-arrow-critical)' : 'url(#dep-arrow)'
					const arrowGlow = isCriticalArrow ? 'url(#dep-glow-critical)' : 'url(#dep-glow)'

					return [
						<path
							key={`${depKey}-hit`}
							d={d}
							fill="none"
							stroke="transparent"
							strokeWidth={14}
							style={{ pointerEvents: 'stroke', cursor: 'pointer' }}
							onMouseEnter={() => setHoveredDep(depKey)}
							onMouseLeave={() => setHoveredDep(null)}
							onClick={(e) => {
								e.stopPropagation()
								setClickedDep({
									fromId: depRelation.fromTaskId,
									toId: depRelation.toTaskId,
									position: { x: e.clientX, y: e.clientY },
								})
							}}
						/>,
						<path
							key={depKey}
							d={d}
							fill="none"
							stroke={isHovered ? arrowHoverColor : arrowColor}
							strokeWidth={1.5}
							strokeDasharray={strokeStyle}
							markerEnd={arrowMarker}
							filter={isHovered ? arrowGlow : undefined}
							style={{ pointerEvents: 'none' }}
						/>,
					]
				})}

				{/* Temporary arrow while linking */}
				{linkingFrom &&
					(() => {
						const pred = tasks.find((t) => t.id === linkingFrom.taskId)
						if (!pred?.start || !pred?.end) return null
						const predIdx = tasks.indexOf(pred)
						const x1 = linkingFrom.side === 'end'
							? (getDayOffset(pred.end, rangeStart) + 1) * dayWidth
							: getDayOffset(pred.start, rangeStart) * dayWidth
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

			{/* Target side selector — appears when linking and hovering over a target task */}
			{linkingFrom && linkTarget && (
				<div
					className="absolute z-20"
					style={{
						left: linkTarget.x - 56,
						top: linkTarget.y - 14,
					}}
					onMouseDown={(e) => e.stopPropagation()}
					onMouseUp={(e) => e.stopPropagation()}
				>
					<div className="flex bg-white dark:bg-zinc-900 rounded-md shadow-lg border border-gray-200 dark:border-zinc-700 overflow-hidden">
						<button
							className="px-3 py-1 text-xs font-medium hover:bg-blue-50 dark:hover:bg-blue-950 text-gray-700 dark:text-zinc-300 transition-colors"
							onClick={() => handleTargetSideClick('start')}
						>
							Start
						</button>
						<div className="w-px bg-gray-200 dark:bg-zinc-700" />
						<button
							className="px-3 py-1 text-xs font-medium hover:bg-blue-50 dark:hover:bg-blue-950 text-gray-700 dark:text-zinc-300 transition-colors"
							onClick={() => handleTargetSideClick('end')}
						>
							Finish
						</button>
					</div>
				</div>
			)}

			{clickedDep && (
				<DependencyActionPopover
					position={clickedDep.position}
					dependencyType={
						dependencies.find(
							(d) => d.fromTaskId === clickedDep.fromId && d.toTaskId === clickedDep.toId
						)?.type || 'FS'
					}
					onChangeType={(newType) => {
						onChangeDependencyType(clickedDep.fromId, clickedDep.toId, newType)
						setClickedDep(null)
					}}
					onDelete={() => {
						onRemoveDependency(clickedDep.fromId, clickedDep.toId)
						setClickedDep(null)
					}}
					onCancel={() => setClickedDep(null)}
				/>
			)}
		</div>
	)
}
