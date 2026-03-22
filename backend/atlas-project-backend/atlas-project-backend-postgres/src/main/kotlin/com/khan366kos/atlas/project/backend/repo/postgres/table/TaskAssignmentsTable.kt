package com.khan366kos.atlas.project.backend.repo.postgres.table

import org.jetbrains.exposed.sql.Table

object TaskAssignmentsTable : Table("task_assignments") {
    val id = uuid("id")
    val projectPlanId = uuid("project_plan_id")
    val taskId = uuid("task_id")
    val resourceId = uuid("resource_id")
    val hoursPerDay = decimal("hours_per_day", 4, 1)
    val plannedEffortHours = decimal("planned_effort_hours", 6, 1).nullable()

    override val primaryKey = PrimaryKey(id)
}
