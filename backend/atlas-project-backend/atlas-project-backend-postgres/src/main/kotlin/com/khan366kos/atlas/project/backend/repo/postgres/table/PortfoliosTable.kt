package com.khan366kos.atlas.project.backend.repo.postgres.table

import org.jetbrains.exposed.sql.Table

object PortfoliosTable : Table("portfolios") {
    val id = uuid("id")
    val name = varchar("name", 255)
    val description = text("description").default("")

    override val primaryKey = PrimaryKey(id)
}
