package com.khan366kos.atlas.project.backend.repo.inmemory

import com.khan366kos.atlas.project.backend.common.models.portfolio.Portfolio
import com.khan366kos.atlas.project.backend.common.models.portfolio.PortfolioId
import com.khan366kos.atlas.project.backend.common.project.PortfolioProject
import com.khan366kos.atlas.project.backend.common.project.PortfolioProjectId
import com.khan366kos.atlas.project.backend.common.project.Project
import com.khan366kos.atlas.project.backend.common.project.ProjectId
import com.khan366kos.atlas.project.backend.common.project.ProjectName
import com.khan366kos.atlas.project.backend.common.project.ProjectPriority
import com.khan366kos.atlas.project.backend.common.repo.IPortfolioRepo
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class PortfolioRepoInMemory : IPortfolioRepo {
    private val portfolios = mutableMapOf<String, Portfolio>()
    private val projects = mutableMapOf<String, Project>()
    private val portfolioProjects = mutableMapOf<String, PortfolioProjectEntry>()

    private data class PortfolioProjectEntry(
        val portfolioId: String,
        val projectId: String,
        val priority: ProjectPriority,
        val sortOrder: Int = 0,
    ) {
        fun toPortfolioProject(id: String) = PortfolioProject(
            id = PortfolioProjectId(id),
            portfolioId = PortfolioId(portfolioId),
            projectId = ProjectId(projectId),
            priority = priority,
        )
    }

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
        portfolioProjects.entries.removeAll { it.value.portfolioId == id }
        return if (portfolios.remove(id) != null) 1 else 0
    }

    // Project operations (independent of portfolios)
    override suspend fun listAllProjects(): List<Project> =
        projects.values.toList()

    override suspend fun getProject(id: String): Project? =
        projects[id]

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun createProject(name: String): Project {
        val newId = Uuid.random().toString()
        val project = Project(
            id = ProjectId(newId),
            name = ProjectName(name),
        )
        projects[newId] = project
        return project
    }

    override suspend fun updateProject(projectId: String, name: String?): Int {
        val existing = projects[projectId] ?: return 0
        projects[projectId] = existing.copy(
            name = ProjectName(name ?: existing.name.asString()),
        )
        return 1
    }

    override suspend fun deleteProject(projectId: String): Int {
        portfolioProjects.entries.removeAll { it.value.projectId == projectId }
        return if (projects.remove(projectId) != null) 1 else 0
    }

    // Portfolio-Project relationship operations
    override suspend fun listPortfolioProjects(portfolioId: String): List<PortfolioProject> =
        portfolioProjects.filter { it.value.portfolioId == portfolioId }
            .entries.sortedBy { it.value.sortOrder }
            .map { (id, entry) -> entry.toPortfolioProject(id) }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun addProjectToPortfolio(
        portfolioId: String,
        projectId: String,
        priority: ProjectPriority,
    ): PortfolioProject {
        val newId = Uuid.random().toString()
        val maxSortOrder = portfolioProjects.values
            .filter { it.portfolioId == portfolioId }
            .maxOfOrNull { it.sortOrder } ?: -1

        val entry = PortfolioProjectEntry(
            portfolioId = portfolioId,
            projectId = projectId,
            priority = priority,
            sortOrder = maxSortOrder + 1,
        )
        portfolioProjects[newId] = entry
        return entry.toPortfolioProject(newId)
    }

    override suspend fun removeProjectFromPortfolio(portfolioId: String, projectId: String): Int {
        val entryToRemove = portfolioProjects.entries.find {
            it.value.portfolioId == portfolioId && it.value.projectId == projectId
        }
        return if (entryToRemove != null) {
            portfolioProjects.remove(entryToRemove.key)
            1
        } else {
            0
        }
    }

    override suspend fun updateProjectPriority(
        portfolioId: String,
        projectId: String,
        priority: ProjectPriority,
    ): Int {
        val entry = portfolioProjects.entries.find {
            it.value.portfolioId == portfolioId && it.value.projectId == projectId
        } ?: return 0
        portfolioProjects[entry.key] = entry.value.copy(priority = priority)
        return 1
    }

    override suspend fun reorderPortfolioProjects(portfolioId: String, orderedProjectIds: List<String>): Int {
        orderedProjectIds.forEachIndexed { index, projectId ->
            val entry = portfolioProjects.entries.find { it.value.projectId == projectId }
            if (entry != null) {
                portfolioProjects[entry.key] = entry.value.copy(sortOrder = index)
            }
        }
        return orderedProjectIds.size
    }
}
