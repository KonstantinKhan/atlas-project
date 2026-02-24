package com.khan366kos.atlas.project.backend.repo.inmemory

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date

object ProjectTasksTable : Table("project_tasks") {
    val id = varchar("id", 255)
    val title = varchar("title", 255)
    val description = text("description")
    val plannedCalendarDuration = integer("planned_calendar_duration").nullable()
    val actualCalendarDuration = integer("actual_calendar_duration").nullable()
    val plannedStartDate = date("planned_start_date").nullable()
    val plannedEndDate = date("planned_end_date").nullable()
    val actualStartDate = date("actual_start_date").nullable()
    val actualEndDate = date("actual_end_date").nullable()
    val status = varchar("status", 50)
    val dependsOn = text("depends_on").default("")
    val dependsOnLag = text("depends_on_lag").default("")
    override val primaryKey = PrimaryKey(id)
}