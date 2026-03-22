package com.khan366kos.atlas.project.backend.repo.postgres.table

import org.jetbrains.exposed.sql.Table

object ProjectsTable : Table("projects") {
    val id = uuid("id")
    val name = varchar("name", 255)
    val portfolioId = uuid("portfolio_id")
    val priority = integer("priority")

    override val primaryKey = PrimaryKey(id)
}
