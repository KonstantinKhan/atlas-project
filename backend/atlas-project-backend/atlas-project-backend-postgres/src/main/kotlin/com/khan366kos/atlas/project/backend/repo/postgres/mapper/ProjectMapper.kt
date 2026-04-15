package com.khan366kos.atlas.project.backend.repo.postgres.mapper

import com.khan366kos.atlas.project.backend.common.models.project.PortfolioProject
import com.khan366kos.atlas.project.backend.common.models.project.ProjectId
import com.khan366kos.atlas.project.backend.common.models.project.ProjectName
import com.khan366kos.atlas.project.backend.common.project.ProjectPriority
import com.khan366kos.atlas.project.backend.repo.postgres.table.PortfolioProjectsTable
import com.khan366kos.atlas.project.backend.repo.postgres.table.ProjectsTable
import org.jetbrains.exposed.sql.ResultRow

internal fun ResultRow.toPortfolioProject() = PortfolioProject(
    id = ProjectId(this[ProjectsTable.id]),
    name = ProjectName(this[ProjectsTable.name]),
    priority = ProjectPriority.entries[this[PortfolioProjectsTable.priority]]
)