package com.khan366kos.config

import com.khan366kos.atlas.project.backend.calendar.service.CacheCalendarProvider
import com.khan366kos.atlas.project.backend.common.repo.IAtlasProjectTaskRepo
import com.khan366kos.configureDatabases
import io.ktor.server.application.ApplicationEnvironment

class AppConfig(environment: ApplicationEnvironment) {
    val repo: IAtlasProjectTaskRepo = configureDatabases(environment)
    val calendarService: CacheCalendarProvider = CacheCalendarProvider(repo)
}