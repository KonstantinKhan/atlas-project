package com.khan366kos

import com.khan366kos.atlas.project.backend.transport.WorkCalendar
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

fun isNonWorkingDay(date: LocalDate, calendar: WorkCalendar): Boolean {
    val dateStr = date.toString()
    if (calendar.workingWeekends?.contains(dateStr) == true) return false
    if (calendar.holidays.contains(dateStr)) return true
    return calendar.weekendDays.contains((date.dayOfWeek.ordinal + 1) % 7)
}

fun countWorkingDays(start: LocalDate, end: LocalDate, calendar: WorkCalendar): Int {
    var count = 0
    var current = start
    while (current <= end) {
        if (!isNonWorkingDay(current, calendar)) count++
        current = current.plus(1, DateTimeUnit.DAY)
    }
    return count
}

fun addWorkingDays(start: LocalDate, days: Int, calendar: WorkCalendar): LocalDate {
    var result = start
    var remaining = if (!isNonWorkingDay(start, calendar)) days - 1 else days
    while (remaining > 0) {
        result = result.plus(1, DateTimeUnit.DAY)
        if (!isNonWorkingDay(result, calendar)) remaining--
    }
    return result
}
