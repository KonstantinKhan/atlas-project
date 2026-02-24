package com.khan366kos

import com.khan366kos.atlas.project.backend.common.repo.IAtlasProjectTaskRepo
import com.khan366kos.atlas.project.backend.repo.inmemory.AtlasProjectTaskRepoInMemory
import io.ktor.server.application.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabases(): IAtlasProjectTaskRepo {
    val url = "jdbc:h2:mem:atlas;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"

    Flyway.configure()
        .dataSource(url, "sa", "")
        .load()
        .migrate()

    val database = Database.connect(url = url, driver = "org.h2.Driver", user = "sa", password = "")
    return AtlasProjectTaskRepoInMemory(database)
}