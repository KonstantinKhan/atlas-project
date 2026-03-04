package com.khan366kos.config

import com.khan366kos.atlas.project.backend.calendar.service.CacheCalendarProvider
import com.khan366kos.atlas.project.backend.common.repo.IAtlasProjectTaskRepo
import com.khan366kos.configureDatabases
import io.ktor.server.application.ApplicationEnvironment

open class AppConfig(
    open val repo: IAtlasProjectTaskRepo,
    open val calendarService: CacheCalendarProvider,
) {
    constructor(environment: ApplicationEnvironment) : this(
        repo = configureDatabases(environment),
    )

    constructor(repo: IAtlasProjectTaskRepo) : this(
        repo = repo,
        calendarService = CacheCalendarProvider(repo),
    )
}