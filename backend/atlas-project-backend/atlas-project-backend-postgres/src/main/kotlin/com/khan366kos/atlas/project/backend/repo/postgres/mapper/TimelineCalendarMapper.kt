package com.khan366kos.atlas.project.backend.repo.postgres.mapper

import com.khan366kos.atlas.project.backend.common.models.timelineCalendar.TimelineCalendar
import com.khan366kos.atlas.project.backend.common.simple.Id
import com.khan366kos.atlas.project.backend.repo.postgres.table.TimelineCalendarTable
import kotlinx.datetime.DayOfWeek
import org.jetbrains.exposed.sql.ResultRow

fun ResultRow.toTimelineCalendar(): TimelineCalendar = TimelineCalendar(
    id = Id(this[TimelineCalendarTable.id].value.toString()),
    workingWeekDays = this[TimelineCalendarTable.workingWeekDays].toDayOfWeekSet(),
    weekendWeekDays = this[TimelineCalendarTable.weekendWeekDays].toDayOfWeekSet(),
)

private fun List<Int>.toDayOfWeekSet(): Set<DayOfWeek> =
    filter { it in 1..7 }
        .map { DayOfWeek(it) }
        .toSet()
