import { Task } from '@/types'
import { WorkCalendar } from '@/types'

const DATE_PADDING_DAYS = 3

export function getCalendarRange(tasks: Task[]): { start: Date; end: Date } {
	const tasksWithDates = tasks.filter(
		(t) => t.plannedStartDate && t.plannedEndDate
	)

	if (tasksWithDates.length === 0) {
		const now = new Date()
		const start = new Date(now.getFullYear(), now.getMonth(), 1)
		const end = new Date(now.getFullYear(), now.getMonth() + 1, 0)
		return { start, end }
	}

	let minDate = new Date(tasksWithDates[0].plannedStartDate!)
	let maxDate = new Date(tasksWithDates[0].plannedEndDate!)

	for (const task of tasksWithDates) {
		const s = new Date(task.plannedStartDate!)
		const e = new Date(task.plannedEndDate!)
		if (s < minDate) minDate = s
		if (e > maxDate) maxDate = e
	}

	const start = new Date(minDate)
	start.setDate(start.getDate() - DATE_PADDING_DAYS)

	const end = new Date(maxDate)
	end.setDate(end.getDate() + DATE_PADDING_DAYS)

	return { start, end }
}

export function getDaysInRange(start: Date, end: Date): Date[] {
	const days: Date[] = []
	const current = new Date(start)
	while (current <= end) {
		days.push(new Date(current))
		current.setDate(current.getDate() + 1)
	}
	return days
}

export function getDayOffset(date: Date, rangeStart: Date): number {
	const msPerDay = 86400000
	const d = new Date(date.getFullYear(), date.getMonth(), date.getDate())
	const s = new Date(
		rangeStart.getFullYear(),
		rangeStart.getMonth(),
		rangeStart.getDate()
	)
	return Math.round((d.getTime() - s.getTime()) / msPerDay)
}

export function groupDaysByMonth(
	days: Date[]
): { month: string; year: number; count: number }[] {
	if (days.length === 0) return []

	const formatter = new Intl.DateTimeFormat('ru-RU', { month: 'long' })
	const groups: { month: string; year: number; count: number }[] = []

	let currentMonth = days[0].getMonth()
	let currentYear = days[0].getFullYear()
	let count = 0

	for (const day of days) {
		if (day.getMonth() === currentMonth && day.getFullYear() === currentYear) {
			count++
		} else {
			groups.push({
				month: formatter.format(new Date(currentYear, currentMonth, 1)),
				year: currentYear,
				count,
			})
			currentMonth = day.getMonth()
			currentYear = day.getFullYear()
			count = 1
		}
	}

	groups.push({
		month: formatter.format(new Date(currentYear, currentMonth, 1)),
		year: currentYear,
		count,
	})

	return groups
}

export function isNonWorkingDay(date: Date, calendar: WorkCalendar): boolean {
	const dateStr = formatDateForInput(date)
	const dayOfWeek = date.getDay()

	if (calendar.workingWeekends?.includes(dateStr)) {
		return false
	}

	if (calendar.holidays.includes(dateStr)) {
		return true
	}

	return calendar.weekendDays.includes(dayOfWeek)
}

export function isSameDay(a: Date, b: Date): boolean {
	return (
		a.getFullYear() === b.getFullYear() &&
		a.getMonth() === b.getMonth() &&
		a.getDate() === b.getDate()
	)
}

export function formatDateForInput(date: Date): string {
	const y = date.getFullYear()
	const m = String(date.getMonth() + 1).padStart(2, '0')
	const d = String(date.getDate()).padStart(2, '0')
	return `${y}-${m}-${d}`
}
