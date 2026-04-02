package com.khan366kos.atlas.project.backend.common.repo

import com.khan366kos.atlas.project.backend.common.models.portfolio.Portfolio
import com.khan366kos.atlas.project.backend.common.project.Project
import com.khan366kos.atlas.project.backend.common.project.ProjectPriority
import com.khan366kos.atlas.project.backend.common.repo.portfolio.DbPortfolioIdRequest
import com.khan366kos.atlas.project.backend.common.repo.portfolio.DbPortfolioRequest
import com.khan366kos.atlas.project.backend.common.repo.portfolio.DbPortfolioResponse
import com.khan366kos.atlas.project.backend.common.repo.portfolio.DbPortfoliosResponse
import com.khan366kos.atlas.project.backend.common.repo.portfolio.PortfolioRepoResult

interface IPortfolioRepo {
    suspend fun createPortfolio(request: DbPortfolioRequest): PortfolioRepoResult
    suspend fun readPortfolio(request: DbPortfolioIdRequest): PortfolioRepoResult
    suspend fun updatePortfolio(request: DbPortfolioRequest): PortfolioRepoResult
    suspend fun deletePortfolio(request: DbPortfolioIdRequest): PortfolioRepoResult
    suspend fun searchPortfolio(): PortfolioRepoResult

    // Project operations (independent of portfolios)
    suspend fun listAllProjects(): List<Project>
    suspend fun getProject(id: String): Project?
    suspend fun createProject(name: String): Project
    suspend fun updateProject(projectId: String, name: String?): Int
    suspend fun deleteProject(projectId: String): Int

    // Portfolio-Project relationship operations
    suspend fun listPortfolioProjects(portfolioId: String): List<Portfolio>
    suspend fun addProjectToPortfolio(portfolioId: String, projectId: String, priority: ProjectPriority): Portfolio
    suspend fun removeProjectFromPortfolio(portfolioId: String, projectId: String): Int
    suspend fun updateProjectPriority(portfolioId: String, projectId: String, priority: ProjectPriority): Int
    suspend fun reorderPortfolioProjects(portfolioId: String, orderedProjectIds: List<String>): Int
}
