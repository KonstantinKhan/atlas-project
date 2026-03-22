package com.khan366kos.atlas.project.backend.repo.postgres

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date

object ProjectTasksTable : Table("project_tasks") {
    val id = uuid("id")
    val projectPlanId = uuid("project_plan_id")
    val title = varchar("title", 255)
    val description = text("description").default("")
    val durationDays = integer("duration_days")
    val status = varchar("status", 50).default("EMPTY")
    val sortOrder = integer("sort_order").default(0)
    val baselineStart = date("baseline_start").nullable()
    val baselineEnd = date("baseline_end").nullable()
    val actualStart = date("actual_start").nullable()
    val actualEnd = date("actual_end").nullable()
    val baselineEffortHours = double("baseline_effort_hours").nullable()
    val additionalEffortHours = double("additional_effort_hours").nullable()
    override val primaryKey = PrimaryKey(id)
}
