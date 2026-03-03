'use client'

import { useRef, useState } from 'react'
import { GanttTask, GanttDependencyDto, TimelineCalendar } from '@/types'
import GanttCalendarBackground from './GanttCalendarBackground'
import GanttTaskLayer from './GanttTaskLayer'

interface GanttCalendarGridProps {
	tasks: GanttTask[]
	dependencies: GanttDependencyDto[]
	days: Date[]
	rangeStart: Date
	dayWidth: number
	rowHeight: number
	timelineCalendar: TimelineCalendar
	onCreateDependency: (fromId: string, toId: string) => void
	onRemoveDependency: (fromId: string, toId: string) => void
}

export default function GanttCalendarGrid({
	tasks,
	dependencies,
	days,
	rangeStart,
	dayWidth,
	rowHeight,
	timelineCalendar,
	onCreateDependency,
	onRemoveDependency,
}: GanttCalendarGridProps) {
	const gridRef = useRef<HTMLDivElement>(null)
	const [linkingFrom, setLinkingFrom] = useState<string | null>(null)
	const [mousePos, setMousePos] = useState({ x: 0, y: 0 })

	return (
		<div
			ref={gridRef}
			style={{
				width: days.length * dayWidth,
				position: 'relative',
				cursor: linkingFrom ? 'crosshair' : undefined,
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
				onRemoveDependency={onRemoveDependency}
				linkingFrom={linkingFrom}
				setLinkingFrom={setLinkingFrom}
				mousePos={mousePos}
				setMousePos={setMousePos}
			/>
		</div>
	)
}
