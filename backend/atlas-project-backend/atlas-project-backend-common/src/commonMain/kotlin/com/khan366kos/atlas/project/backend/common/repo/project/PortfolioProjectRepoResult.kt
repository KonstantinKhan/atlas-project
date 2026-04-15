package com.khan366kos.atlas.project.backend.common.repo.project

import com.khan366kos.atlas.project.backend.common.models.project.PortfolioProject
import com.khan366kos.atlas.project.backend.common.models.project.Project

sealed class PortfolioProjectRepoResult {
    data class Single(val project: Project) : PortfolioProjectRepoResult()
    data class Multiple(val projects: List<PortfolioProject>) : PortfolioProjectRepoResult()
    data object NotFound : PortfolioProjectRepoResult()
    data class DbError(val cause: Throwable) : PortfolioProjectRepoResult()
}