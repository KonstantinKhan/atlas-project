package com.khan366kos.atlas.project.backend.common.project

import com.khan366kos.atlas.project.backend.common.models.portfolio.PortfolioId

data class Project(
    val id: ProjectId = ProjectId.NONE,
    val name: ProjectName = ProjectName.NONE,
    val portfolioId: PortfolioId = PortfolioId.NONE,
    val priority: ProjectPriority = ProjectPriority.MEDIUM,
    val sortOrder: Int = 0,
)
