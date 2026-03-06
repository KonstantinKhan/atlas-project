package com.khan366kos

import com.khan366kos.atlas.project.backend.common.models.ProjectDate
import com.khan366kos.atlas.project.backend.common.models.simple.Duration
import com.khan366kos.atlas.project.backend.common.models.taskSchedule.TaskSchedule
import com.khan366kos.atlas.project.backend.common.models.taskSchedule.TaskScheduleId
import com.khan366kos.atlas.project.backend.common.models.task.simple.TaskId
import com.khan366kos.atlas.project.backend.mappers.toDto
import com.khan366kos.atlas.project.backend.mappers.toGanttDto
import com.khan366kos.atlas.project.backend.mappers.toTransport
import com.khan366kos.atlas.project.backend.mappers.toDomain
import com.khan366kos.atlas.project.backend.transport.commands.ChangeTaskStartDateCommandDto
import com.khan366kos.atlas.project.backend.transport.commands.CreateDependencyCommandDto
import com.khan366kos.atlas.project.backend.transport.commands.CreateTaskInPoolCommandDto
import com.khan366kos.atlas.project.backend.transport.CreateProjectTaskRequest
import com.khan366kos.atlas.project.backend.transport.UpdateProjectTaskRequest
import com.khan366kos.atlas.project.backend.transport.commands.ChangeTaskEndDateCommandDto
import com.khan366kos.atlas.project.backend.transport.commands.AssignScheduleCommandDto
import com.khan366kos.atlas.project.backend.transport.commands.ChangeDependencyTypeCommandDto
import com.khan366kos.atlas.project.backend.transport.commands.PlanFromEndCommandDto
import kotlinx.datetime.LocalDate
import com.khan366kos.config.AppConfig
import io.ktor.http.HttpStatusCode
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.delete
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

        post("/resize-from-start") {
            val request = call.receive<ChangeTaskStartDateCommandDto>()
            val plan = config.repo.projectPlan()
            val delta = plan.resizeTaskFromStart(
                taskId = TaskId(request.taskId),
                newStart = request.newPlannedStart,
                calendar = config.calendarService.current()
            )
            delta.updatedSchedule.forEach {
                plan.schedules()[it.id] = it
                config.repo.updateSchedule(it)
            }
            plan.tasks().find { it.id == TaskId(request.taskId) }
                ?.let { config.repo.updateTask(it) }
            call.respond(delta.toDto())
        }

        post("/change-end") {
            val request = call.receive<ChangeTaskEndDateCommandDto>()
            val plan = config.repo.projectPlan()
            val delta = plan.changeTaskEndDate(
                taskId = TaskId(request.taskId),
                newEnd = request.newPlannedEnd,
                calendar = config.calendarService.current()
            )
            delta.updatedSchedule.forEach {
                plan.schedules()[it.id] = it
                config.repo.updateSchedule(it)
            }
            plan.tasks().find { it.id == TaskId(request.taskId) }
                ?.let { config.repo.updateTask(it) }
            call.respond(delta.toDto())
        }

        post("/dependencies") {
            val request = call.receive<CreateDependencyCommandDto>()
            val plan = config.repo.projectPlan()
            val calendar = config.calendarService.current()
            
            val delta = plan.addDependency(
                predecessorId = TaskId(request.fromTaskId),
                successorId = TaskId(request.toTaskId),
                type = request.type.toDomain(),
                lagDays = request.lagDays,
                calendar = calendar
            )
            
            // Persist the dependency and updated schedules
            val computedLag = plan.dependencies()
                .find { it.predecessor == TaskId(request.fromTaskId) && it.successor == TaskId(request.toTaskId) }
                ?.lag?.asInt() ?: 0
            config.repo.addDependency(
                predecessorId = request.fromTaskId,
                successorId = request.toTaskId,
                type = request.type.name,
                lagDays = computedLag
            )
            delta.updatedSchedule.forEach {
                plan.schedules()[it.id] = it
                config.repo.updateSchedule(it)
            }
            
            // Return the updated plan
            call.respond(plan.toGanttDto())
        }

        patch("/dependencies") {
            val request = call.receive<ChangeDependencyTypeCommandDto>()
            val plan = config.repo.projectPlan()
            val calendar = config.calendarService.current()

            val delta = plan.changeDependencyType(
                predecessorId = TaskId(request.fromTaskId),
                successorId = TaskId(request.toTaskId),
                newType = request.newType.toDomain(),
                calendar = calendar
            )

            // Persist updated dependency type and lag
            val updatedDep = plan.dependencies()
                .find { it.predecessor == TaskId(request.fromTaskId) && it.successor == TaskId(request.toTaskId) }
            if (updatedDep != null) {
                config.repo.updateDependency(
                    predecessorId = request.fromTaskId,
                    successorId = request.toTaskId,
                    type = updatedDep.type.name,
                    lagDays = updatedDep.lag.asInt()
                )
            }
            delta.updatedSchedule.forEach { config.repo.updateSchedule(it) }

            call.respond(plan.toGanttDto())
        }

        delete("/dependencies") {
            val from = call.request.queryParameters["from"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing 'from' parameter")
            val to = call.request.queryParameters["to"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing 'to' parameter")

            val plan = config.repo.projectPlan()
            val calendar = config.calendarService.current()

            val delta = plan.removeDependency(
                predecessorId = TaskId(from),
                successorId = TaskId(to),
                calendar = calendar
            )

            config.repo.deleteDependency(from, to)
            delta.updatedSchedule.forEach { config.repo.updateSchedule(it) }

            call.respond(plan.toGanttDto())
        }

        post("/dependencies/recalculate") {
            val plan = config.repo.projectPlan()
            val calendar = config.calendarService.current()
            val delta = plan.recalculateAll(calendar)
            delta.updatedSchedule.forEach { config.repo.updateSchedule(it) }
            call.respond(config.repo.projectPlan().toGanttDto())
        }

        post("/project-tasks/{id}/schedule") {
            val id = call.parameters["id"]
                ?: return@post call.respond(HttpStatusCode.BadRequest, "Task ID parameter is missing")

            if (id.isBlank()) {
                return@post call.respond(HttpStatusCode.BadRequest, "Task ID cannot be empty")
            }

            config.repo.getTask(id)
                ?: return@post call.respond(HttpStatusCode.NotFound)

            val request = call.receive<AssignScheduleCommandDto>()
            val calendar = config.calendarService.current()
            val startDate = calendar.currentOrNextWorkingDay(LocalDate.parse(request.start))
            val endDate = calendar.addWorkingDays(startDate, Duration(request.duration))

            config.repo.updateSchedule(
                TaskSchedule(
                    id = TaskScheduleId(id),
                    start = ProjectDate.Set(startDate),
                    end = ProjectDate.Set(endDate),
                )
            )
            call.respond(config.repo.projectPlan().toGanttDto())
        }

        post("/plan-from-end") {
            val request = call.receive<PlanFromEndCommandDto>()
            val plan = config.repo.projectPlan()
            val calendar = config.calendarService.current()
            val delta = plan.planFromEnd(
                taskId = TaskId(request.taskId),
                newEnd = LocalDate.parse(request.newPlannedEnd),
                calendar = calendar
            )
            delta.updatedSchedule.forEach {
                config.repo.updateSchedule(it)
            }
            call.respond(plan.toGanttDto())
        }

        post("/project-tasks") {
            try {
                val request = call.receive<CreateProjectTaskRequest>()
                val created = config.repo.createTask(request.toModel())

                val calendar = config.calendarService.current()
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                val startDate = calendar.currentOrNextWorkingDay(today)
                val duration = created.duration.let {
                    if (it == Duration.NONE || it.asInt() <= 0) Duration(1) else it
                }
                val endDate = calendar.addWorkingDays(startDate, duration)

                config.repo.updateSchedule(
                    TaskSchedule(
                        id = TaskScheduleId(created.id.value),
                        start = ProjectDate.Set(startDate),
                        end = ProjectDate.Set(endDate),
                    )
                )
                call.respond(HttpStatusCode.Created, created.toScheduledTaskDto(startDate, endDate))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        post("/project-tasks/create-in-pool") {
            try {
                val request = call.receive<CreateTaskInPoolCommandDto>()
                val created = config.repo.createTaskWithoutSchedule(request.toModel())
                call.respond(HttpStatusCode.Created, created.toTaskDto())
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }
        delete("/project-tasks/{id}") {
            val id = call.parameters["id"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Task ID parameter is missing")

            if (id.isBlank()) {
                return@delete call.respond(HttpStatusCode.BadRequest, "Task ID cannot be empty")
            }

            config.repo.getTask(id)
                ?: return@delete call.respond(HttpStatusCode.NotFound)

            config.repo.deleteTask(id)
            call.respond(HttpStatusCode.NoContent)
        }
        patch("/project-tasks/{id}") {
            try {
                val id = call.parameters["id"]
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, "Task ID parameter is missing")

                if (id.isBlank()) {
                    return@patch call.respond(HttpStatusCode.BadRequest, "Task ID cannot be empty")
                }

                val request = call.receive<UpdateProjectTaskRequest>()

                val existing = config.repo.getTask(id)
                    ?: return@patch call.respond(HttpStatusCode.NotFound)

                val updated = existing.applyUpdate(request)
                call.respond(config.repo.updateTask(updated).toTaskDto())
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }
    }
}
