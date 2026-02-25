package com.khan366kos

import com.khan366kos.atlas.project.backend.common.models.ProjectDate
import com.khan366kos.atlas.project.backend.common.repo.IAtlasProjectTaskRepo
import com.khan366kos.atlas.project.backend.mappers.toTransport
import com.khan366kos.atlas.project.backend.transport.CreateProjectTaskRequest
import com.khan366kos.atlas.project.backend.transport.UpdateProjectTaskRequest
import com.khan366kos.atlas.project.backend.transport.WorkCalendar
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.routing

fun Application.configureRouting(repo: IAtlasProjectTaskRepo) {
    routing {
        get("/work-calendar") {
            val timelineCalendar = repo.timelineCalendar().toTransport()
            call.respond(timelineCalendar)
        }
        get("/project-tasks") {
            call.respond(repo.tasks().map { it.toTransport() })
        }
        post("/project-tasks") {
            val request = call.receive<CreateProjectTaskRequest>()
            val created = repo.createTask(request.toModel())
            call.respond(HttpStatusCode.Created, created.toTransport())
        }
        patch("/project-tasks/{id}") {
            val id = call.parameters["id"]
                ?: return@patch call.respond(HttpStatusCode.BadRequest, "Task ID parameter is missing")

            if (id.isBlank()) {
                return@patch call.respond(HttpStatusCode.BadRequest, "Task ID cannot be empty")
            }

            val request = call.receive<UpdateProjectTaskRequest>()
            val calendar = WorkCalendar()

            val existing = repo.getTask(id)
                ?: return@patch call.respond(HttpStatusCode.NotFound)

            val newStart = request.plannedStartDate
            val resolvedPlannedEndDate = if (
                newStart != null && request.plannedEndDate == null
            ) {
                val existingStart = (existing.plannedStartDate as? ProjectDate.Set)?.date
                val existingEnd = (existing.plannedEndDate as? ProjectDate.Set)?.date
                if (existingStart != null && existingEnd != null) {
                    val workingDays = countWorkingDays(existingStart, existingEnd, calendar)
                    addWorkingDays(newStart, workingDays, calendar)
                } else null
            } else {
                request.plannedEndDate
            }

            val updated = existing.applyUpdate(request, resolvedPlannedEndDate)
            call.respond(repo.updateTask(updated).toTransport())
        }
    }
}