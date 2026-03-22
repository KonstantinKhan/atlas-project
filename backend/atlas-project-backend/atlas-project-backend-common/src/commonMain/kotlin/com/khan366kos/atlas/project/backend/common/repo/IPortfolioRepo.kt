package com.khan366kos.atlas.project.backend.common.repo

import com.khan366kos.atlas.project.backend.common.models.portfolio.Portfolio
import com.khan366kos.atlas.project.backend.common.project.Project

interface IPortfolioRepo {
    suspend fun listPortfolios(): List<Portfolio>
    suspend fun getPortfolio(id: String): Portfolio?
    suspend fun createPortfolio(portfolio: Portfolio): Portfolio
    suspend fun updatePortfolio(portfolio: Portfolio): Portfolio
    suspend fun deletePortfolio(id: String): Int
    suspend fun listProjects(portfolioId: String): List<Project>
    suspend fun getProject(id: String): Project?
    suspend fun createProject(portfolioId: String, name: String, priority: Int): Project
    suspend fun updateProject(projectId: String, name: String?, priority: Int?): Int
    suspend fun deleteProject(projectId: String): Int
    suspend fun reorderProjects(portfolioId: String, orderedProjectIds: List<String>): Int
    suspend fun listAllProjects(): List<Project>
}
