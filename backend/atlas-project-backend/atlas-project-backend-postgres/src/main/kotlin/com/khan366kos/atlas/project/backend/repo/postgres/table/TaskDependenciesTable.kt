package com.khan366kos.atlas.project.backend.repo.postgres.table

import org.jetbrains.exposed.dao.id.IntIdTable

object TaskDependenciesTable : IntIdTable("task_dependencies") {
    val projectPlanId = uuid("project_plan_id")
    val predecessorTaskId = uuid("predecessor_task_id")
    val successorTaskId = uuid("successor_task_id")
    val type = text("type")
    val lagDays = integer("lag_days").default(0)
}
