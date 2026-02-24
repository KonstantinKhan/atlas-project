'use client'

import { useRef, useState } from 'react'
import { Task, WorkCalendar } from '@/types'
import { isNonWorkingDay, isSameDay, getDayOffset } from '@/utils/ganttDateUtils'
import { ganttDayCell } from './GanttChart.styles'
import GanttBar from './GanttBar'

interface GanttCalendarGridProps {
	tasks: Task[]
	days: Date[]
	rangeStart: Date
	dayWidth: number
	rowHeight: number
	workCalendar: WorkCalendar
	onCreateDependency: (fromId: string, toId: string) => void
	onRemoveDependency: (fromId: string, toId: string) => void
}

export default function GanttCalendarGrid({
	tasks,
	days,
	rangeStart,
	dayWidth,
	rowHeight,
	workCalendar,
	onCreateDependency,
	onRemoveDependency,
}: GanttCalendarGridProps) {
	const today = new Date()
	const gridRef = useRef<HTMLDivElement>(null)
	const [linkingFrom, setLinkingFrom] = useState<string | null>(null)
	const [mousePos, setMousePos] = useState({ x: 0, y: 0 })

	const handleMouseMove = (e: React.MouseEvent) => {
		if (!linkingFrom || !gridRef.current) return
		const rect = gridRef.current.getBoundingClientRect()
		setMousePos({ x: e.clientX - rect.left, y: e.clientY - rect.top })
	}

	const handleMouseUp = (e: React.MouseEvent) => {
		if (!linkingFrom || !gridRef.current) return
		const rect = gridRef.current.getBoundingClientRect()
		const rowIndex = Math.floor((e.clientY - rect.top) / rowHeight)
		if (rowIndex >= 0 && rowIndex < tasks.length) {
			const target = tasks[rowIndex]
			if (target.id !== linkingFrom) {
				onCreateDependency(linkingFrom, target.id)
			}
		}
		setLinkingFrom(null)
	}

	return (
		<div
			ref={gridRef}
			style={{
				width: days.length * dayWidth,
				position: 'relative',
				cursor: linkingFrom ? 'crosshair' : undefined,
			}}
			onMouseMove={handleMouseMove}
			onMouseUp={handleMouseUp}
			onMouseLeave={() => setLinkingFrom(null)}
		>
			{tasks.map((task) => (
				<div
					key={task.id}
					className="relative flex"
					style={{ height: rowHeight }}
				>
					{days.map((day) => {
						const isT = isSameDay(day, today)
						const isNW = isNonWorkingDay(day, workCalendar)
						return (
							<div
								key={day.toISOString()}
								className={ganttDayCell({ isNonWorking: isNW, isToday: isT })}
								style={{ width: dayWidth, minWidth: dayWidth }}
							/>
						)
					})}
					<GanttBar
						task={task}
						rangeStart={rangeStart}
						dayWidth={dayWidth}
						onLinkStart={(id, e) => {
							e.preventDefault()
							setLinkingFrom(id)
						}}
						isLinkTarget={linkingFrom !== null && linkingFrom !== task.id}
					/>
				</div>
			))}

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
				{tasks.flatMap((dep) =>
					(dep.dependsOn ?? []).map((predId) => {
						const pred = tasks.find((t) => t.id === predId)
						if (!pred?.plannedEndDate || !dep.plannedStartDate) return null
						const predIdx = tasks.indexOf(pred)
						const depIdx = tasks.indexOf(dep)
						const x1 =
							(getDayOffset(pred.plannedEndDate, rangeStart) + 1) * dayWidth
						const y1 = predIdx * rowHeight + rowHeight / 2
						const x2 = getDayOffset(dep.plannedStartDate, rangeStart) * dayWidth
						const y2 = depIdx * rowHeight + rowHeight / 2
						const ARM = 12
						const cornerX = x2 - ARM
						let d: string
						if (cornerX >= x1 + ARM) {
							d = `M${x1},${y1} L${cornerX},${y1} L${cornerX},${y2} L${x2},${y2}`
						} else {
							const wrapRight = x1 + ARM
							const wrapLeft = x2 - ARM
							const boundaryY =
								Math.min(predIdx, depIdx) * rowHeight + rowHeight
							d = `M${x1},${y1} L${wrapRight},${y1} L${wrapRight},${boundaryY} L${wrapLeft},${boundaryY} L${wrapLeft},${y2} L${x2},${y2}`
						}
						return (
							<path
								key={`${predId}-${dep.id}`}
								d={d}
								fill="none"
								stroke="#6366f1"
								strokeWidth={1.5}
								strokeDasharray="4 2"
								markerEnd="url(#dep-arrow)"
								style={{ pointerEvents: 'stroke', cursor: 'pointer' }}
								onClick={() => onRemoveDependency(predId, dep.id)}
							/>
						)
					}),
				)}

				{/* Temporary arrow while dragging */}
				{linkingFrom &&
					(() => {
						const pred = tasks.find((t) => t.id === linkingFrom)
						if (!pred?.plannedEndDate) return null
						const predIdx = tasks.indexOf(pred)
						const x1 =
							(getDayOffset(pred.plannedEndDate, rangeStart) + 1) * dayWidth
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
