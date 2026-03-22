package com.khan366kos.atlas.project.backend.repo.postgres

import com.khan366kos.atlas.project.backend.common.models.portfolio.Portfolio
import com.khan366kos.atlas.project.backend.common.models.portfolio.PortfolioId
import com.khan366kos.atlas.project.backend.common.repo.IPortfolioRepo
import com.khan366kos.atlas.project.backend.repo.postgres.table.PortfoliosTable
import com.khan366kos.atlas.project.backend.repo.postgres.table.ProjectPlansTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
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

    override suspend fun listPortfolios(): List<Portfolio> = newSuspendedTransaction(db = database) {
        PortfoliosTable.selectAll()
            .map { it.toPortfolio() }
    }

    override suspend fun getPortfolio(id: String): Portfolio? = newSuspendedTransaction(db = database) {
        PortfoliosTable.selectAll()
            .where { PortfoliosTable.id eq UUID.fromString(id) }
            .singleOrNull()
            ?.toPortfolio()
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun createPortfolio(portfolio: Portfolio): Portfolio = newSuspendedTransaction(db = database) {
        val newId = Uuid.random().toJavaUuid()
        PortfoliosTable.insert {
            it[id] = newId
            it[name] = portfolio.name
            it[description] = portfolio.description
        }
        portfolio.copy(id = PortfolioId(newId.toString()))
    }

    override suspend fun updatePortfolio(portfolio: Portfolio): Portfolio = newSuspendedTransaction(db = database) {
        PortfoliosTable.update({ PortfoliosTable.id eq UUID.fromString(portfolio.id.value) }) {
            it[name] = portfolio.name
            it[description] = portfolio.description
        }
        portfolio
    }

    override suspend fun deletePortfolio(id: String): Int = newSuspendedTransaction(db = database) {
        PortfoliosTable.deleteWhere { PortfoliosTable.id eq UUID.fromString(id) }
    }

    override suspend fun listProjectIds(portfolioId: String): List<String> = newSuspendedTransaction(db = database) {
        ProjectPlansTable.selectAll()
            .where { ProjectPlansTable.portfolioId eq UUID.fromString(portfolioId) }
            .orderBy(ProjectPlansTable.priority)
            .map { it[ProjectPlansTable.id].toString() }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun createProject(portfolioId: String, name: String, priority: Int): String =
        newSuspendedTransaction(db = database) {
            val newId = Uuid.random().toJavaUuid()
            ProjectPlansTable.insert {
                it[id] = newId
                it[ProjectPlansTable.name] = name
                it[ProjectPlansTable.portfolioId] = UUID.fromString(portfolioId)
                it[ProjectPlansTable.priority] = priority
            }
            newId.toString()
        }

    override suspend fun updateProject(projectId: String, name: String?, priority: Int?): Int =
        newSuspendedTransaction(db = database) {
            ProjectPlansTable.update({ ProjectPlansTable.id eq UUID.fromString(projectId) }) {
                if (name != null) it[ProjectPlansTable.name] = name
                if (priority != null) it[ProjectPlansTable.priority] = priority
            }
        }

    override suspend fun deleteProject(projectId: String): Int = newSuspendedTransaction(db = database) {
        ProjectPlansTable.deleteWhere { ProjectPlansTable.id eq UUID.fromString(projectId) }
    }

    override suspend fun reorderProjects(portfolioId: String, orderedProjectIds: List<String>): Int =
        newSuspendedTransaction(db = database) {
            orderedProjectIds.forEachIndexed { index, id ->
                ProjectPlansTable.update({ ProjectPlansTable.id eq UUID.fromString(id) }) {
                    it[priority] = index
                }
            }
            orderedProjectIds.size
        }

    override suspend fun listAllProjectIds(): List<Pair<String, String>> = newSuspendedTransaction(db = database) {
        ProjectPlansTable.selectAll()
            .map { it[ProjectPlansTable.portfolioId].toString() to it[ProjectPlansTable.id].toString() }
    }

    private fun ResultRow.toPortfolio() = Portfolio(
        id = PortfolioId(this[PortfoliosTable.id].toString()),
        name = this[PortfoliosTable.name],
        description = this[PortfoliosTable.description],
    )
}
