package com.khan366kos.atlas.project.backend.repo.postgres.mapper

import com.khan366kos.atlas.project.backend.common.project.Project
import com.khan366kos.atlas.project.backend.common.project.ProjectId
import com.khan366kos.atlas.project.backend.common.project.ProjectName
import com.khan366kos.atlas.project.backend.repo.postgres.table.ProjectsTable
import org.jetbrains.exposed.sql.ResultRow

internal fun ResultRow.toProject() = Project(
    id = ProjectId(this[ProjectsTable.id]),
    name = ProjectName(this[ProjectsTable.name]),
)