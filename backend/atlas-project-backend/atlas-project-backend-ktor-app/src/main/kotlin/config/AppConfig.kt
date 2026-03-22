package com.khan366kos.config

import com.khan366kos.DatabaseRepos
import com.khan366kos.atlas.project.backend.calendar.service.CacheCalendarProvider
import com.khan366kos.atlas.project.backend.common.repo.IAtlasProjectTaskRepo
import com.khan366kos.atlas.project.backend.common.repo.IPortfolioRepo
import com.khan366kos.atlas.project.backend.common.repo.IResourceRepo
import com.khan366kos.atlas.project.backend.project.service.ProjectService
import com.khan366kos.configureDatabases
import io.ktor.server.application.ApplicationEnvironment

class AppConfig(
    val repo: IAtlasProjectTaskRepo,
    val resourceRepo: IResourceRepo,
    val portfolioRepo: IPortfolioRepo,
    val calendarService: CacheCalendarProvider,
    val projectService: ProjectService = ProjectService(repo)
) {
    constructor(environment: ApplicationEnvironment) : this(
        repos = configureDatabases(environment),
    )

    constructor(repo: IAtlasProjectTaskRepo, resourceRepo: IResourceRepo, portfolioRepo: IPortfolioRepo) : this(
        repo = repo,
        resourceRepo = resourceRepo,
        portfolioRepo = portfolioRepo,
        calendarService = CacheCalendarProvider(repo),
    )

    private constructor(repos: DatabaseRepos) : this(
        repo = repos.taskRepo,
        resourceRepo = repos.resourceRepo,
        portfolioRepo = repos.portfolioRepo,
        calendarService = CacheCalendarProvider(repos.taskRepo),
    )
}
