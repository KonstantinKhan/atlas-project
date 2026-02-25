package com.khan366kos.atlas.project.backend.repo.postgres.table

import org.jetbrains.exposed.sql.Table

object ProjectPlansTable : Table("project_plans") {
    val id = uuid("id")
    override val primaryKey = PrimaryKey(id)
}
