package com.khan366kos

import com.khan366kos.atlas.project.backend.common.repo.IAtlasProjectTaskRepo
import com.khan366kos.atlas.project.backend.transport.WorkCalendar
import io.ktor.server.application.*
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting(repo: IAtlasProjectTaskRepo) {
    routing {
        get("/work-calendar") {
            call.respond(WorkCalendar())
        }
        get("/project-tasks") {
            call.respond(repo.tasks().map { it.toTransport() })
        }
    }
}