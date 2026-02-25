package com.khan366kos.atlas.project.backend.repo.postgres.table

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.date

object TimelineCalendarHolidaysTable : IntIdTable("timeline_calendar_holidays") {
    val calendarId = integer("calendar_id").references(TimelineCalendarTable.id, onDelete = ReferenceOption.CASCADE)
    val holidayDate = date("holiday_date")

    init {
        uniqueIndex(calendarId, holidayDate)
    }
}
