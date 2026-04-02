package com.khan366kos.atlas.project.backend.common.models.portfolio

data class ProjectPortfolio(
    val id: ProjectPortfolioId,
    val name: String,
    val description: String,
) {
    companion object {
        val NONE = ProjectPortfolio(
            id = ProjectPortfolioId.NONE,
            name = "",
            description = "",
        )
    }
}
