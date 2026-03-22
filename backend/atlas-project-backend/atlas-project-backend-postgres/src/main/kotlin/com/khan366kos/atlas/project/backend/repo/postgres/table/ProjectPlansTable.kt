package com.khan366kos.atlas.project.backend.repo.postgres.table

import org.jetbrains.exposed.sql.Table

object ProjectPlansTable : Table("project_plans") {
    val id = uuid("id")
    val name = varchar("name", 255).default("Default Project")
    val portfolioId = uuid("portfolio_id")
    val priority = integer("priority").default(0)

    override val primaryKey = PrimaryKey(id)
}
