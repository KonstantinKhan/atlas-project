'use client'

import { isNonWorkingDay, isToday, getWeekLabel } from '@/utils/ganttDateUtils'
import { TimelineCalendar } from '@/types'
import { ganttHeaderDay } from './GanttChart.styles'
import type { ViewMode } from '@/store/timelineCalendarStore'

interface GanttCalendarHeaderProps {
	days: Date[]
	weeks: Date[]
	monthGroups: { month: string; year: number; count: number }[]
	dayWidth: number
	headerHeight: number
	workCalendar: TimelineCalendar
	viewMode: ViewMode
}

export default function GanttCalendarHeader({
	days,
	weeks,
	monthGroups,
	dayWidth,
	headerHeight,
	workCalendar,
	viewMode,
}: GanttCalendarHeaderProps) {
	const weekWidth = dayWidth * 7
	const totalWidth = viewMode === 'day'
		? days.length * dayWidth
		: weeks.length * weekWidth

	const monthUnitWidth = viewMode === 'day' ? dayWidth : weekWidth

	return (
		<div style={{ width: totalWidth, height: headerHeight }} className="flex flex-col">
			{/* Month row */}
			<div className="flex flex-1 border-b border-gray-200 dark:border-zinc-800">
				{monthGroups.map((group, i) => (
					<div
						key={`${group.month}-${group.year}-${i}`}
						className="text-xs font-semibold text-gray-700 dark:text-zinc-300 border-r border-gray-200 dark:border-zinc-800 px-2 py-1.5 truncate capitalize"
						style={{ width: group.count * monthUnitWidth }}
					>
						{group.month} {group.year}
					</div>
				))}
			</div>

			{/* Day / Week row */}
			<div className="flex flex-1 border-b border-gray-300 dark:border-zinc-700">
				{viewMode === 'day'
					? days.map((day) => {
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
					})
					: weeks.map((week) => (
						<div
							key={week.toISOString()}
							className="text-[10px] text-center text-gray-500 dark:text-zinc-400 border-r border-gray-200 dark:border-zinc-800 truncate"
							style={{ width: weekWidth, lineHeight: `${28}px` }}
						>
							{getWeekLabel(week)}
						</div>
					))
				}
			</div>
		</div>
	)
}
