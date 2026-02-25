package com.khan366kos.atlas.project.backend.common.models.timelineCalendar

import com.khan366kos.atlas.project.backend.common.simple.Id
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

data class TimelineCalendar(
    val id: Id = Id.NONE,
    val workingWeekDays: Set<DayOfWeek> = emptySet(),
    val weekendWeekDays: Set<DayOfWeek> = emptySet(),
    val holidays: Set<LocalDate> = emptySet(),
    val workingWeekends: Set<LocalDate> = emptySet(),
)