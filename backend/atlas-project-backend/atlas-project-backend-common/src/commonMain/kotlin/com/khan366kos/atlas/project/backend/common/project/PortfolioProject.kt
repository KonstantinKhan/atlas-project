package com.khan366kos.atlas.project.backend.common.project

import com.khan366kos.atlas.project.backend.common.models.portfolio.PortfolioId

data class PortfolioProject(
    val id: PortfolioProjectId = PortfolioProjectId.NONE,
    val portfolioId: PortfolioId = PortfolioId.NONE,
    val projectId: ProjectId = ProjectId.NONE,
    val priority: ProjectPriority = ProjectPriority.MEDIUM,
) {
    companion object {
        val NONE = PortfolioProject()
    }
}
