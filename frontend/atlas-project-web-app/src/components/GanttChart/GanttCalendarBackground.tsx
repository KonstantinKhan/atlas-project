'use client'

import { isNonWorkingDay, isToday } from '@/utils/ganttDateUtils'
import { TimelineCalendar } from '@/types'
import { ganttDayCell } from './GanttChart.styles'
import type { ViewMode } from '@/store/timelineCalendarStore'

interface GanttCalendarBackgroundProps {
	days: Date[]
	rowHeight: number
	totalRows: number
	dayWidth: number
	timelineCalendar: TimelineCalendar
	viewMode?: ViewMode
}

export default function GanttCalendarBackground({
	days,
	rowHeight,
	totalRows,
	dayWidth,
	timelineCalendar,
	viewMode = 'day',
}: GanttCalendarBackgroundProps) {
	const totalHeight = totalRows * rowHeight

	if (viewMode === 'week') {
		const weekWidth = dayWidth * 7
		const weekCount = Math.ceil(days.length / 7)
		return (
			<div
				style={{
					width: days.length * dayWidth,
					height: totalHeight,
					position: 'absolute',
					top: 0,
					left: 0,
				}}
			>
				{Array.from({ length: weekCount }, (_, i) => (
					<div
						key={i}
						style={{
							width: weekWidth,
							height: totalHeight,
							position: 'absolute',
							left: i * weekWidth,
							top: 0,
						}}
						className={`border-r border-gray-200 dark:border-zinc-800 ${i % 2 === 1 ? 'bg-gray-50/50 dark:bg-zinc-900/30' : ''}`}
					/>
				))}
			</div>
		)
	}

	return (
		<div
			style={{
				width: days.length * dayWidth,
				height: totalHeight,
				position: 'absolute',
				top: 0,
				left: 0,
			}}
		>
			{days.map((day) => {
				const isT = isToday(day)
				const isNonWorking = isNonWorkingDay(day, timelineCalendar)
				return (
					<div
						key={day.toISOString()}
						className={ganttDayCell({ isNonWorking: isNonWorking, isToday: isT })}
						style={{
							width: dayWidth,
							minWidth: dayWidth,
							height: totalHeight,
							position: 'absolute',
							left: days.indexOf(day) * dayWidth,
							top: 0,
						}}
					/>
				)
			})}
		</div>
	)
}
