package com.khan366kos.atlas.project.backend.repo.postgres

import org.jetbrains.exposed.sql.Table

object ProjectTasksTable : Table("project_tasks") {
    val id = uuid("id")
    val projectPlanId = uuid("project_plan_id")
    val title = varchar("title", 255)
    val description = text("description").default("")
    val durationDays = integer("duration_days")
    val status = varchar("status", 50).default("EMPTY")
    override val primaryKey = PrimaryKey(id)
}
