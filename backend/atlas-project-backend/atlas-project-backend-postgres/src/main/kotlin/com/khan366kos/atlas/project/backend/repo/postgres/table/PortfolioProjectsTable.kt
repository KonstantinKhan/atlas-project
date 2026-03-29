package com.khan366kos.atlas.project.backend.repo.postgres.table

import org.jetbrains.exposed.sql.Table

object PortfolioProjectsTable : Table("portfolio_projects") {
    val id = uuid("id")
    val portfolioId = reference("portfolio_id", PortfoliosTable.id)
    val projectId = reference("project_id", ProjectsTable.id)
    val priority = integer("priority")
    val sortOrder = integer("sort_order").default(0)

    override val primaryKey = PrimaryKey(id)
}
