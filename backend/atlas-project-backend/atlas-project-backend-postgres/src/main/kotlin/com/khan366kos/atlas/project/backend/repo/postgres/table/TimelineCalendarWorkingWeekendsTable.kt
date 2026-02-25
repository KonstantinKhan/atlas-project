package com.khan366kos.atlas.project.backend.repo.postgres.table

import com.khan366kos.atlas.project.backend.repo.postgres.table.TimelineCalendarTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.date

object TimelineCalendarWorkingWeekendsTable : IntIdTable("timeline_calendar_working_weekends") {
    val calendarId = integer("calendar_id").references(TimelineCalendarTable.id, onDelete = ReferenceOption.CASCADE)
    val workingDate = date("working_date")

    init {
        uniqueIndex(calendarId, workingDate)
    }
}
