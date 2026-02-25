package com.khan366kos.atlas.project.backend.repo.postgres.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date

object TaskSchedulesTable : Table("task_schedules") {
    val taskId = uuid("task_id")
    val plannedStart = date("planned_start")
    val plannedEnd = date("planned_end")
    override val primaryKey = PrimaryKey(taskId)
}
