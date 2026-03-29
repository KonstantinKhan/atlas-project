package com.khan366kos.atlas.project.backend.common.repo

import com.khan366kos.atlas.project.backend.common.models.portfolio.Portfolio
import com.khan366kos.atlas.project.backend.common.project.PortfolioProject
import com.khan366kos.atlas.project.backend.common.project.Project
import com.khan366kos.atlas.project.backend.common.project.ProjectPriority

interface IPortfolioRepo {
    // Portfolio operations
    suspend fun listPortfolios(): List<Portfolio>
    suspend fun getPortfolio(id: String): Portfolio?
    suspend fun createPortfolio(portfolio: Portfolio): Portfolio
    suspend fun updatePortfolio(portfolio: Portfolio): Portfolio
    suspend fun deletePortfolio(id: String): Int

    // Project operations (independent of portfolios)
    suspend fun listAllProjects(): List<Project>
    suspend fun getProject(id: String): Project?
    suspend fun createProject(name: String): Project
    suspend fun updateProject(projectId: String, name: String?): Int
    suspend fun deleteProject(projectId: String): Int

    // Portfolio-Project relationship operations
    suspend fun listPortfolioProjects(portfolioId: String): List<PortfolioProject>
    suspend fun addProjectToPortfolio(portfolioId: String, projectId: String, priority: ProjectPriority): PortfolioProject
    suspend fun removeProjectFromPortfolio(portfolioId: String, projectId: String): Int
    suspend fun updateProjectPriority(portfolioId: String, projectId: String, priority: ProjectPriority): Int
    suspend fun reorderPortfolioProjects(portfolioId: String, orderedProjectIds: List<String>): Int
}
