package com.khan366kos.atlas.project.backend.repo.postgres.table

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object TimelineCalendarTable : IntIdTable("timeline_calendar") {
    val name = varchar("name", 255)
    val workingWeekDays = array<Int>("working_week_days")
    val weekendWeekDays = array<Int>("weekend_week_days")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}
