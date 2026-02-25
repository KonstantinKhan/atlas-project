'use client'

import { isNonWorkingDay, isToday } from '@/utils/ganttDateUtils'
import { TimelineCalendar } from '@/types'
import { ganttDayCell } from './GanttChart.styles'

interface GanttCalendarBackgroundProps {
	days: Date[]
	rowHeight: number
	totalRows: number
	dayWidth: number
	timelineCalendar: TimelineCalendar
}

export default function GanttCalendarBackground({
	days,
	rowHeight,
	totalRows,
	dayWidth,
	timelineCalendar,
}: GanttCalendarBackgroundProps) {
	return (
		<div
			style={{
				width: days.length * dayWidth,
				height: totalRows * rowHeight,
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
							height: rowHeight * totalRows,
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
