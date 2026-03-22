package com.khan366kos.atlas.project.backend.repo.postgres.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date

object AssignmentDayOverridesTable : Table("assignment_day_overrides") {
    val id = integer("id").autoIncrement()
    val assignmentId = uuid("assignment_id")
    val overrideDate = date("override_date")
    val hours = decimal("hours", 4, 1)

    override val primaryKey = PrimaryKey(id)
}
