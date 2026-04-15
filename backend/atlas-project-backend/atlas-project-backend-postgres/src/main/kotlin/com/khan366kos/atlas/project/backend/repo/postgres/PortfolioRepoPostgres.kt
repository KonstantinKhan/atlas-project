package com.khan366kos.atlas.project.backend.repo.postgres

import com.khan366kos.atlas.project.backend.common.models.portfolio.Portfolio
import com.khan366kos.atlas.project.backend.common.models.portfolio.PortfolioId
import com.khan366kos.atlas.project.backend.common.models.project.Project
import com.khan366kos.atlas.project.backend.common.models.project.ProjectId
import com.khan366kos.atlas.project.backend.common.models.project.ProjectName
import com.khan366kos.atlas.project.backend.common.project.ProjectPriority
import com.khan366kos.atlas.project.backend.common.repo.portfolio.IPortfolioRepo
import com.khan366kos.atlas.project.backend.common.repo.portfolio.DbPortfolioIdRequest
import com.khan366kos.atlas.project.backend.common.repo.portfolio.DbPortfolioRequest
import com.khan366kos.atlas.project.backend.common.repo.portfolio.PortfolioRepoResult
import com.khan366kos.atlas.project.backend.repo.postgres.mapper.toPortfolioProject
import com.khan366kos.atlas.project.backend.repo.postgres.table.PortfolioProjectsTable
import com.khan366kos.atlas.project.backend.repo.postgres.table.PortfoliosTable
import com.khan366kos.atlas.project.backend.repo.postgres.table.ProjectPlansTable
import com.khan366kos.atlas.project.backend.repo.postgres.table.ProjectsTable
import kotlinx.coroutines.CancellationException
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

class PortfolioRepoPostgres(private val database: Database) : IPortfolioRepo {

    override suspend fun searchPortfolio(): PortfolioRepoResult =
        newSuspendedTransaction(db = database) {
            runCatching {
                PortfoliosTable.selectAll().map {
                    it.toPortfolio()
                }
            }
                .onFailure { if (it is CancellationException) throw it }
                .fold(
                    onSuccess = {
                        PortfolioRepoResult.Multiple(it)
                    },
                    onFailure = {
                        PortfolioRepoResult.DbError(it)
                    }
                )
        }

    override suspend fun readPortfolio(request: DbPortfolioIdRequest): PortfolioRepoResult =
        newSuspendedTransaction(db = database) {
            runCatching {
                PortfoliosTable.selectAll()
                    .where { PortfoliosTable.id eq request.id.asUUID() }
                    .singleOrNull()
                    ?.toPortfolio()
            }
                .onFailure { if (it is CancellationException) throw it }
                .fold(
                    onSuccess = { portfolio ->
                        portfolio
                            ?.let { PortfolioRepoResult.Multiple(listOf(it)) }
                            ?: PortfolioRepoResult.NotFound
                    },
                    onFailure = {
                        PortfolioRepoResult.DbError(it)
                    }
                )
        }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun createPortfolio(request: DbPortfolioRequest): PortfolioRepoResult =
        newSuspendedTransaction(db = database) {
            runCatching {
                val newId = Uuid.random().toJavaUuid()
                PortfoliosTable.insert {
                    it[id] = newId
                    it[name] = request.portfolio.name
                    it[description] = request.portfolio.description
                }
                request.portfolio.copy(id = PortfolioId(newId))
            }
                .onFailure { if (it is CancellationException) throw it }
                .fold(
                    onSuccess = {
                        PortfolioRepoResult.Single(it)
                    },
                    onFailure = {
                        PortfolioRepoResult.DbError(it)
                    }
                )
        }

    override suspend fun updatePortfolio(request: DbPortfolioRequest): PortfolioRepoResult =
        newSuspendedTransaction(db = database) {
            runCatching {
                val updateCount = PortfoliosTable.update({ PortfoliosTable.id eq request.portfolio.id.asUUID() }) {
                    request.portfolio.name.takeIf { name -> name.isNotBlank() }
                        ?.let { portfolioName -> it[name] = portfolioName }
                    request.portfolio.description.takeIf { description -> description.isNotBlank() }
                        ?.let { portfolioDescription -> it[description] = portfolioDescription }
                }

                if (updateCount == 0) return@runCatching null
                request.portfolio
            }
                .onFailure { if (it is CancellationException) throw it }
                .fold(
                    onSuccess = { portfolio ->
                        portfolio
                            ?.let { PortfolioRepoResult.Single(it) }
                            ?: PortfolioRepoResult.NotFound
                    },
                    onFailure = {
                        PortfolioRepoResult.DbError(it)
                    }
                )
        }

    override suspend fun deletePortfolio(request: DbPortfolioIdRequest): PortfolioRepoResult =
        newSuspendedTransaction(db = database) {
            runCatching {
                val response = PortfoliosTable.selectAll()
                    .where { PortfoliosTable.id eq request.id.asUUID() }
                    .singleOrNull()
                    ?.toPortfolio()
                PortfoliosTable.deleteWhere { PortfoliosTable.id eq request.id.asUUID() }
                response
            }
                .onFailure { if (it is CancellationException) throw it }
                .fold(
                    onSuccess = { portfolio ->
                        portfolio
                            ?.let { PortfolioRepoResult.Single(it) }
                            ?: PortfolioRepoResult.NotFound
                    },
                    onFailure = {
                        PortfolioRepoResult.DbError(it)
                    }
                )
        }

    // Project operations (independent of portfolios)
    override suspend fun listAllProjects(): List<Project> = newSuspendedTransaction(db = database) {
        ProjectsTable.selectAll().map { it.toProject() }
    }

    override suspend fun getProject(id: String): Project? = newSuspendedTransaction(db = database) {
        ProjectsTable.selectAll()
            .where { ProjectsTable.id eq UUID.fromString(id) }
            .singleOrNull()
            ?.toProject()
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun createProject(name: String): Project = newSuspendedTransaction(db = database) {
        val newId = Uuid.random().toJavaUuid()
        ProjectsTable.insert {
            it[id] = newId
            it[ProjectsTable.name] = name
        }
        ProjectPlansTable.insert {
            it[id] = newId
            it[projectId] = newId
        }
        Project(
            id = ProjectId(newId.toString()),
            name = ProjectName(name),
        )
    }

    override suspend fun updateProject(projectId: String, name: String?): Int =
        newSuspendedTransaction(db = database) {
            ProjectsTable.update({ ProjectsTable.id eq UUID.fromString(projectId) }) {
                if (name != null) it[ProjectsTable.name] = name
            }
        }

    override suspend fun deleteProject(projectId: String): Int = newSuspendedTransaction(db = database) {
        ProjectsTable.deleteWhere { ProjectsTable.id eq UUID.fromString(projectId) }
    }

    // Portfolio-Project relationship operations
    override suspend fun listPortfolioProjects(portfolioId: String): List<Portfolio> =
        newSuspendedTransaction(db = database) {
            PortfolioProjectsTable.selectAll()
                .where { PortfolioProjectsTable.portfolioId eq UUID.fromString(portfolioId) }
                .orderBy(PortfolioProjectsTable.sortOrder)
                .map { it.toPortfolio() }
        }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun addProjectToPortfolio(
        portfolioId: String,
        projectId: String,
        priority: ProjectPriority,
    ): Portfolio = newSuspendedTransaction(db = database) {
        val newId = Uuid.random().toJavaUuid()
        // Get the max sort order for this portfolio
        val maxSortOrder = PortfolioProjectsTable
            .selectAll()
            .where { PortfolioProjectsTable.portfolioId eq UUID.fromString(portfolioId) }
            .maxOfOrNull { it[PortfolioProjectsTable.sortOrder] } ?: -1

        PortfolioProjectsTable.insert {
            it[id] = newId
            it[PortfolioProjectsTable.portfolioId] = UUID.fromString(portfolioId)
            it[PortfolioProjectsTable.projectId] = UUID.fromString(projectId)
            it[PortfolioProjectsTable.priority] = priority.ordinal
            it[PortfolioProjectsTable.sortOrder] = maxSortOrder + 1
        }
        Portfolio(
            id = PortfolioId(newId.toString()),
            name = "new Project",
            description = "",
        )
    }

    override suspend fun removeProjectFromPortfolio(portfolioId: String, projectId: String): Int =
        newSuspendedTransaction(db = database) {
            PortfolioProjectsTable.deleteWhere {
                (PortfolioProjectsTable.portfolioId eq UUID.fromString(portfolioId)) and
                        (PortfolioProjectsTable.projectId eq UUID.fromString(projectId))
            }
        }

    override suspend fun updateProjectPriority(
        portfolioId: String,
        projectId: String,
        priority: ProjectPriority,
    ): Int = newSuspendedTransaction(db = database) {
        PortfolioProjectsTable.update({
            (PortfolioProjectsTable.portfolioId eq UUID.fromString(portfolioId)) and
                    (PortfolioProjectsTable.projectId eq UUID.fromString(projectId))
        }) {
            it[PortfolioProjectsTable.priority] = priority.ordinal
        }
    }

    override suspend fun reorderPortfolioProjects(portfolioId: String, orderedProjectIds: List<String>): Int =
        newSuspendedTransaction(db = database) {
            orderedProjectIds.forEachIndexed { index, id ->
                PortfolioProjectsTable.update({ PortfolioProjectsTable.projectId eq UUID.fromString(id) }) {
                    it[sortOrder] = index
                }
            }
            orderedProjectIds.size
        }

    private fun ResultRow.toPortfolio() = Portfolio(
        id = PortfolioId(this[PortfoliosTable.id].toString()),
        name = this[PortfoliosTable.name],
        description = this[PortfoliosTable.description],
    )

    private fun ResultRow.toProject() = Project(
        id = ProjectId(this[ProjectsTable.id]),
        name = ProjectName(this[ProjectsTable.name]),
    )
}
