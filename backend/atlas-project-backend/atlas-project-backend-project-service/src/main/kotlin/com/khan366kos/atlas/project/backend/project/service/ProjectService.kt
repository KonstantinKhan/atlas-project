package com.khan366kos.atlas.project.backend.project.service

import com.khan366kos.atlas.project.backend.common.exceptions.ProjectOperationFailedException
import com.khan366kos.atlas.project.backend.common.models.portfolio.PortfolioId
import com.khan366kos.atlas.project.backend.common.models.project.PortfolioProject
import com.khan366kos.atlas.project.backend.common.repo.project.DbProjectSearchRequest
import com.khan366kos.atlas.project.backend.common.repo.project.IProjectRepo
import com.khan366kos.atlas.project.backend.common.repo.project.PortfolioProjectRepoResult

class ProjectService(
    private val repo: IProjectRepo
) {
    suspend fun portfolioProjects(portfolioId: PortfolioId): List<PortfolioProject> {
        return when (val result = repo.search(DbProjectSearchRequest(portfolioId))) {
            is PortfolioProjectRepoResult.Multiple -> result.projects
            is PortfolioProjectRepoResult.NotFound ->
                throw ProjectOperationFailedException(RuntimeException("No portfolio found with id: $portfolioId"))
            is PortfolioProjectRepoResult.DbError -> throw ProjectOperationFailedException(result.cause)
            else -> emptyList()
        }
    }
}