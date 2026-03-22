package com.khan366kos.atlas.project.backend.repo.postgres.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object ResourcesTable : Table("resources") {
    val id = uuid("id")
    val name = varchar("name", 255)
    val type = varchar("type", 20)
    val capacityHoursPerDay = decimal("capacity_hours_per_day", 4, 1)
    val sortOrder = integer("sort_order").default(0)
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}
