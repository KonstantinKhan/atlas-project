package com.khan366kos.atlas.project.backend.transport.timelineCalendar

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class TimelineCalendarDto(
    val workingWeekDays: Set<DayOfWeek>,
    val weekendWeekDays: Set<DayOfWeek>,
    val holidays: Set<LocalDate>,
    val workingWeekends: Set<LocalDate>
)