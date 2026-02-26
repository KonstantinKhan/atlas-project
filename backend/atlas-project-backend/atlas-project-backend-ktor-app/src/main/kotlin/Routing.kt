package com.khan366kos

import com.khan366kos.atlas.project.backend.common.models.task.simple.TaskId
import com.khan366kos.atlas.project.backend.common.repo.IAtlasProjectTaskRepo
import com.khan366kos.atlas.project.backend.mappers.toDto
import com.khan366kos.atlas.project.backend.mappers.toGanttDto
import com.khan366kos.atlas.project.backend.mappers.toTransport
import com.khan366kos.atlas.project.backend.transport.ChangeTaskStartDateCommandDto
import com.khan366kos.atlas.project.backend.transport.CreateProjectTaskRequest
import com.khan366kos.atlas.project.backend.transport.UpdateProjectTaskRequest
import com.khan366kos.config.AppConfig
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.routing

fun Application.configureRouting(config: AppConfig) {
    routing {
        get("/work-calendar") {
            val timelineCalendar = config.calendarService.current()
            call.respond(timelineCalendar.toTransport())
        }
        get("/project-plan") {
            val plan = config.repo.projectPlan()
            call.respond(plan.toGanttDto())
        }

        post("/change-start") {
            val request = call.receive<ChangeTaskStartDateCommandDto>()
            val plan = config.repo.projectPlan()
            val delta = plan.changeTaskStartDate(
                taskId = TaskId(request.taskId),
                newStart = request.newPlannedStart,
                calendar = config.calendarService.current()
            )
            delta.updatedSchedule.forEach {
                plan.schedules()[it.id] = it
                config.repo.updateSchedule(it)
            }
            call.respond(delta.toDto())
        }

        post("/project-tasks") {
            val request = call.receive<CreateProjectTaskRequest>()
            val created = config.repo.createTask(request.toModel())
            call.respond(HttpStatusCode.Created, created.toTransport())
        }
        patch("/project-tasks/{id}") {
            val id = call.parameters["id"]
                ?: return@patch call.respond(HttpStatusCode.BadRequest, "Task ID parameter is missing")

            if (id.isBlank()) {
                return@patch call.respond(HttpStatusCode.BadRequest, "Task ID cannot be empty")
            }

            val request = call.receive<UpdateProjectTaskRequest>()

            val existing = config.repo.getTask(id)
                ?: return@patch call.respond(HttpStatusCode.NotFound)

            val updated = existing.applyUpdate(request)
            call.respond(config.repo.updateTask(updated).toTransport())
        }
    }
}
