'use client'

import { isNonWorkingDay, isToday } from '@/utils/ganttDateUtils'
import { TimelineCalendar } from '@/types'
import { ganttHeaderDay } from './GanttChart.styles'

interface GanttCalendarHeaderProps {
	days: Date[]
	monthGroups: { month: string; year: number; count: number }[]
	dayWidth: number
	headerHeight: number
	workCalendar: TimelineCalendar
}

export default function GanttCalendarHeader({
	days,
	monthGroups,
	dayWidth,
	headerHeight,
	workCalendar,
}: GanttCalendarHeaderProps) {
	return (
		<div style={{ width: days.length * dayWidth, height: headerHeight }} className="flex flex-col">
			{/* Month row */}
			<div className="flex flex-1 border-b border-gray-200 dark:border-zinc-800">
				{monthGroups.map((group, i) => (
					<div
						key={`${group.month}-${group.year}-${i}`}
						className="text-xs font-semibold text-gray-700 dark:text-zinc-300 border-r border-gray-200 dark:border-zinc-800 px-2 py-1.5 truncate capitalize"
						style={{ width: group.count * dayWidth }}
					>
						{group.month} {group.year}
					</div>
				))}
			</div>

			{/* Day number row */}
			<div className="flex flex-1 border-b border-gray-300 dark:border-zinc-700">
				{days.map((day) => {
					const isT = isToday(day)
					const isNW = isNonWorkingDay(day, workCalendar)
					return (
						<div
							key={day.toISOString()}
							className={ganttHeaderDay({ isNonWorking: isNW, isToday: isT })}
							style={{ width: dayWidth, lineHeight: `${28}px` }}
						>
							{day.getDate()}
						</div>
					)
				})}
			</div>
		</div>
	)
}
