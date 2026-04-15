package com.khan366kos.atlas.project.backend.repo.postgres

import com.khan366kos.atlas.project.backend.common.repo.project.DbProjectSearchRequest
import com.khan366kos.atlas.project.backend.common.repo.project.IProjectRepo
import com.khan366kos.atlas.project.backend.common.repo.project.PortfolioProjectRepoResult
import com.khan366kos.atlas.project.backend.repo.postgres.mapper.toPortfolioProject
import com.khan366kos.atlas.project.backend.repo.postgres.table.PortfolioProjectsTable
import com.khan366kos.atlas.project.backend.repo.postgres.table.ProjectsTable
import kotlinx.coroutines.CancellationException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class ProjectRepoPostgres(private val database: Database) : IProjectRepo {
    override suspend fun search(request: DbProjectSearchRequest): PortfolioProjectRepoResult =
        newSuspendedTransaction {
            runCatching {
                ProjectsTable
                    .innerJoin(PortfolioProjectsTable)
                    .selectAll()
                    .where { PortfolioProjectsTable.portfolioId eq request.portfolioId.asUUID() }
                    .toList()
            }
                .onFailure { if (it is CancellationException) throw it }
                .fold(
                    onSuccess = {
                        if (it.isEmpty()) PortfolioProjectRepoResult.NotFound
                        else
                            PortfolioProjectRepoResult.Multiple(it.map { entity -> entity.toPortfolioProject() })
                    },
                    onFailure = {
                        PortfolioProjectRepoResult.DbError(it)
                    }
                )
        }
}