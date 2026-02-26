package com.khan366kos.atlas.project.backend.common.models.timelineCalendar

import com.khan366kos.atlas.project.backend.common.models.simple.Duration
import com.khan366kos.atlas.project.backend.common.simple.Id
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

data class TimelineCalendar(
    val id: Id = Id.NONE,
    val workingWeekDays: Set<DayOfWeek> = emptySet(),
    val weekendWeekDays: Set<DayOfWeek> = emptySet(),
    val holidays: Set<LocalDate> = emptySet(),
    val workingWeekends: Set<LocalDate> = emptySet(),
) {
    private fun isWorkingDay(date: LocalDate): Boolean =
        (date.dayOfWeek in workingWeekDays ||
                date in workingWeekends) && date !in holidays

    private fun nextWorkingDay(date: LocalDate): LocalDate {
        var d = date.plus(1, DateTimeUnit.DAY)
        while (!isWorkingDay(d)) {
            d = d.plus(1, DateTimeUnit.DAY)
        }
        return d
    }

    fun addWorkingDays(start: LocalDate, duration: Duration): LocalDate {
        if (duration.asInt() <= 0) return start

        var result = start
        var remaining = if (isWorkingDay(start)) duration.asInt() - 1 else duration.asInt()

        while (remaining > 0) {
            result = result.plus(1, DateTimeUnit.DAY)
            if (isWorkingDay(result)) remaining--
        }

        return result
    }

    fun subtractWorkingDays(end: LocalDate, duration: Duration): LocalDate {
        if (duration.asInt() <= 0) return end
        var result = end
        var remaining = if (isWorkingDay(end)) duration.asInt() - 1 else duration.asInt()
        while (remaining > 0) {
            result = result.plus(-1, DateTimeUnit.DAY)
            if (isWorkingDay(result)) remaining--
        }
        return result
    }
}