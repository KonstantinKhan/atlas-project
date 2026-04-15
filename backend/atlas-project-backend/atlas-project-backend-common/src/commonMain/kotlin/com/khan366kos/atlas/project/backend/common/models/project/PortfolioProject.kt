package com.khan366kos.atlas.project.backend.common.models.project

import com.khan366kos.atlas.project.backend.common.project.ProjectPriority

data class PortfolioProject(
    val id: ProjectId,
    val name: ProjectName,
    val priority: ProjectPriority
)
