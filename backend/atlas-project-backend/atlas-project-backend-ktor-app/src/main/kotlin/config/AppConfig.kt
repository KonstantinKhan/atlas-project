package com.khan366kos.config

import com.khan366kos.DatabaseRepos
import com.khan366kos.atlas.project.backend.calendar.service.CacheCalendarProvider
import com.khan366kos.atlas.project.backend.common.repo.IAtlasProjectTaskRepo
import com.khan366kos.atlas.project.backend.common.repo.portfolio.IPortfolioRepo
import com.khan366kos.atlas.project.backend.common.repo.IResourceRepo
import com.khan366kos.atlas.project.backend.common.repo.project.IProjectRepo
import com.khan366kos.atlas.project.backend.portfolio.service.PortfolioService
import com.khan366kos.atlas.project.backend.project.service.ProjectService
import com.khan366kos.configureDatabases
import io.ktor.server.application.ApplicationEnvironment

class AppConfig(
    val repo: IAtlasProjectTaskRepo,
    val resourceRepo: IResourceRepo,
    val portfolioRepo: IPortfolioRepo,
    val projectRepo: IProjectRepo,
    val calendarService: CacheCalendarProvider,
    val projectService: ProjectService = ProjectService(projectRepo),
    val portfolioService: PortfolioService = PortfolioService(portfolioRepo),
) {
    constructor(environment: ApplicationEnvironment) : this(
        repos = configureDatabases(environment),
    )

    constructor(
        repo: IAtlasProjectTaskRepo,
        resourceRepo: IResourceRepo,
        portfolioRepo: IPortfolioRepo,
        projectRepo: IProjectRepo
    ) : this(
        repo = repo,
        resourceRepo = resourceRepo,
        portfolioRepo = portfolioRepo,
        projectRepo = projectRepo,
        calendarService = CacheCalendarProvider(repo),
    )

    private constructor(repos: DatabaseRepos) : this(
        repo = repos.taskRepo,
        resourceRepo = repos.resourceRepo,
        portfolioRepo = repos.portfolioRepo,
        projectRepo = repos.projectRepo,
        calendarService = CacheCalendarProvider(repos.taskRepo),
    )
}
