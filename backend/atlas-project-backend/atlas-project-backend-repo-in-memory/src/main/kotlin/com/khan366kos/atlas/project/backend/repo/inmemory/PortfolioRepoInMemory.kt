package com.khan366kos.atlas.project.backend.repo.inmemory

import com.khan366kos.atlas.project.backend.common.models.portfolio.Portfolio
import com.khan366kos.atlas.project.backend.common.models.portfolio.PortfolioId
import com.khan366kos.atlas.project.backend.common.repo.IPortfolioRepo
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class PortfolioRepoInMemory : IPortfolioRepo {
    private val portfolios = mutableMapOf<String, Portfolio>()
    private val projects = mutableMapOf<String, ProjectEntry>()

    private data class ProjectEntry(
        val portfolioId: String,
        val name: String,
        val priority: Int,
    )

    override suspend fun listPortfolios(): List<Portfolio> =
        portfolios.values.toList()

    override suspend fun getPortfolio(id: String): Portfolio? =
        portfolios[id]

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun createPortfolio(portfolio: Portfolio): Portfolio {
        val newId = Uuid.random().toString()
        val created = portfolio.copy(id = PortfolioId(newId))
        portfolios[newId] = created
        return created
    }

    override suspend fun updatePortfolio(portfolio: Portfolio): Portfolio {
        portfolios[portfolio.id.value] = portfolio
        return portfolio
    }

    override suspend fun deletePortfolio(id: String): Int {
        projects.entries.removeAll { it.value.portfolioId == id }
        return if (portfolios.remove(id) != null) 1 else 0
    }

    override suspend fun listProjectIds(portfolioId: String): List<String> =
        projects.filter { it.value.portfolioId == portfolioId }
            .entries.sortedBy { it.value.priority }
            .map { it.key }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun createProject(portfolioId: String, name: String, priority: Int): String {
        val newId = Uuid.random().toString()
        projects[newId] = ProjectEntry(portfolioId = portfolioId, name = name, priority = priority)
        return newId
    }

    override suspend fun updateProject(projectId: String, name: String?, priority: Int?): Int {
        val existing = projects[projectId] ?: return 0
        projects[projectId] = existing.copy(
            name = name ?: existing.name,
            priority = priority ?: existing.priority,
        )
        return 1
    }

    override suspend fun deleteProject(projectId: String): Int =
        if (projects.remove(projectId) != null) 1 else 0

    override suspend fun listAllProjectIds(): List<Pair<String, String>> =
        projects.map { (projectId, entry) -> entry.portfolioId to projectId }

    override suspend fun reorderProjects(portfolioId: String, orderedProjectIds: List<String>): Int {
        orderedProjectIds.forEachIndexed { index, id ->
            val existing = projects[id] ?: return@forEachIndexed
            projects[id] = existing.copy(priority = index)
        }
        return orderedProjectIds.size
    }
}
