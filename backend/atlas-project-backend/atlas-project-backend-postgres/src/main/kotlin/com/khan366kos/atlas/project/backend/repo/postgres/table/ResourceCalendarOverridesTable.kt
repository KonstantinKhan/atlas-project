package com.khan366kos.atlas.project.backend.repo.postgres.table

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.date

object ResourceCalendarOverridesTable : IntIdTable("resource_calendar_overrides") {
    val resourceId = uuid("resource_id")
    val overrideDate = date("override_date")
    val availableHours = decimal("available_hours", 4, 1)

    init {
        uniqueIndex(resourceId, overrideDate)
    }
}
