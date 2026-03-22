package com.khan366kos

import com.khan366kos.atlas.project.backend.common.repo.IAtlasProjectTaskRepo
import com.khan366kos.atlas.project.backend.common.repo.IPortfolioRepo
import com.khan366kos.atlas.project.backend.common.repo.IResourceRepo
import com.khan366kos.atlas.project.backend.repo.postgres.AtlasProjectTaskRepoPostgres
import com.khan366kos.atlas.project.backend.repo.postgres.PortfolioRepoPostgres
import com.khan366kos.atlas.project.backend.repo.postgres.ResourceRepoPostgres
import io.ktor.server.application.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

data class DatabaseRepos(
    val taskRepo: IAtlasProjectTaskRepo,
    val resourceRepo: IResourceRepo,
    val portfolioRepo: IPortfolioRepo,
)

fun configureDatabases(environment: ApplicationEnvironment): DatabaseRepos {
    val dbUrl = environment.config.property("postgres.url").getString()
    val dbUser = environment.config.property("postgres.user").getString()
    val dbPassword = environment.config.property("postgres.password").getString()

    Flyway.configure()
        .dataSource(dbUrl, dbUser, dbPassword)
        .load()
        .migrate()

    val database = Database.connect(url = dbUrl, driver = "org.postgresql.Driver", user = dbUser, password = dbPassword)
    return DatabaseRepos(
        taskRepo = AtlasProjectTaskRepoPostgres(database),
        resourceRepo = ResourceRepoPostgres(database),
        portfolioRepo = PortfolioRepoPostgres(database),
    )
}
