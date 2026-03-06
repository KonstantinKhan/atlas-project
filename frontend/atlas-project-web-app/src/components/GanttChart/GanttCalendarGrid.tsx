'use client'

import { useRef, useState } from 'react'
import { GanttTask, GanttDependencyDto, TimelineCalendar } from '@/types'
import GanttCalendarBackground from './GanttCalendarBackground'
import GanttTaskLayer from './GanttTaskLayer'
import { useDroppable } from '@dnd-kit/react'

export type LinkSide = 'start' | 'end'

interface GanttCalendarGridProps {
	tasks: GanttTask[]
	dependencies: GanttDependencyDto[]
	days: Date[]
	rangeStart: Date
	dayWidth: number
	rowHeight: number
	timelineCalendar: TimelineCalendar
	onCreateDependency: (fromId: string, toId: string, type: string) => void
	onChangeDependencyType: (fromId: string, toId: string, newType: string) => void
	onRemoveDependency: (fromId: string, toId: string) => void
	onMoveTask: (taskId: string, newStartDate: string) => void
	onResizeTask: (taskId: string, newEndDate: string) => void
	onResizeFromStart: (taskId: string, newStartDate: string) => void
}

export const TIMELINE_DROP_ID = 'timeline-grid'

export default function GanttCalendarGrid({
	tasks,
	dependencies,
	days,
	rangeStart,
	dayWidth,
	rowHeight,
	timelineCalendar,
	onCreateDependency,
	onChangeDependencyType,
	onRemoveDependency,
	onMoveTask,
	onResizeTask,
	onResizeFromStart,
}: GanttCalendarGridProps) {
	const gridRef = useRef<HTMLDivElement>(null)
	const [linkingFrom, setLinkingFrom] = useState<{ taskId: string; side: LinkSide } | null>(null)
	const [mousePos, setMousePos] = useState({ x: 0, y: 0 })

	const { ref: dropRef, isDropTarget } = useDroppable({
		id: TIMELINE_DROP_ID,
		element: gridRef,
	})

	return (
		<div
			ref={(node) => {
				gridRef.current = node
				dropRef(node)
			}}
			style={{
				width: days.length * dayWidth,
				position: 'relative',
				cursor: linkingFrom ? 'crosshair' : undefined,
				outline: isDropTarget ? '2px dashed #6366f1' : undefined,
			}}
		>
			<GanttCalendarBackground
				days={days}
				rowHeight={rowHeight}
				totalRows={tasks.length}
				dayWidth={dayWidth}
				timelineCalendar={timelineCalendar}
			/>

			<GanttTaskLayer
				tasks={tasks}
				dependencies={dependencies}
				days={days}
				rangeStart={rangeStart}
				dayWidth={dayWidth}
				rowHeight={rowHeight}
				onCreateDependency={onCreateDependency}
				onChangeDependencyType={onChangeDependencyType}
				onRemoveDependency={onRemoveDependency}
				onMoveTask={onMoveTask}
				onResizeTask={onResizeTask}
				onResizeFromStart={onResizeFromStart}
				linkingFrom={linkingFrom}
				setLinkingFrom={setLinkingFrom}
				mousePos={mousePos}
				setMousePos={setMousePos}
			/>
		</div>
	)
}
