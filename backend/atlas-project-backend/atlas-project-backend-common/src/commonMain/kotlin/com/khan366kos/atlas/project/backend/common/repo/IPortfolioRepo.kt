package com.khan366kos.atlas.project.backend.common.repo

import com.khan366kos.atlas.project.backend.common.models.portfolio.Portfolio

interface IPortfolioRepo {
    suspend fun listPortfolios(): List<Portfolio>
    suspend fun getPortfolio(id: String): Portfolio?
    suspend fun createPortfolio(portfolio: Portfolio): Portfolio
    suspend fun updatePortfolio(portfolio: Portfolio): Portfolio
    suspend fun deletePortfolio(id: String): Int
    suspend fun listProjectIds(portfolioId: String): List<String>
    suspend fun createProject(portfolioId: String, name: String, priority: Int): String
    suspend fun updateProject(projectId: String, name: String?, priority: Int?): Int
    suspend fun deleteProject(projectId: String): Int
    suspend fun reorderProjects(portfolioId: String, orderedProjectIds: List<String>): Int
    suspend fun listAllProjectIds(): List<Pair<String, String>>
}
