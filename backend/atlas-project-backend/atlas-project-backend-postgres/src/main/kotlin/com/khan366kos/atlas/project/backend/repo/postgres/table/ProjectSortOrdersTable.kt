package com.khan366kos.atlas.project.backend.repo.postgres.table

import org.jetbrains.exposed.sql.Table

object ProjectSortOrdersTable : Table("project_sort_orders") {
    val portfolioId = uuid("portfolio_id")
    val projectId = reference("project_id", ProjectsTable.id)
    val sortOrder = integer("sort_order").default(0)

    override val primaryKey = PrimaryKey(portfolioId, projectId)
}
