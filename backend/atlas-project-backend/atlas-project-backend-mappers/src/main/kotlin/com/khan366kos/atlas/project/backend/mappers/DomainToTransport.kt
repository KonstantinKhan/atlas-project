package com.khan366kos.atlas.project.backend.mappers

import com.khan366kos.atlas.project.backend.common.models.timelineCalendar.TimelineCalendar
import com.khan366kos.atlas.project.backend.transport.timelineCalendar.TimelineCalendarDto

fun TimelineCalendar.toTransport() = TimelineCalendarDto(
    workingWeekDays = workingWeekDays,
    weekendWeekDays = weekendWeekDays,
    holidays = holidays,
    workingWeekends = workingWeekends
)