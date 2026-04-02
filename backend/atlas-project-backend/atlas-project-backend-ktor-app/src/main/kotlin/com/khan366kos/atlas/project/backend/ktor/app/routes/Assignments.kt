package com.khan366kos.atlas.project.backend.ktor.app.routes

import com.khan366kos.atlas.project.backend.common.models.resource.AssignmentDayOverride
import com.khan366kos.atlas.project.backend.common.models.resource.AssignmentId
import com.khan366kos.atlas.project.backend.common.models.resource.ResourceId
import com.khan366kos.atlas.project.backend.common.models.resource.ResourceLoadCalculator
import com.khan366kos.atlas.project.backend.common.models.resource.TaskAssignment
import com.khan366kos.atlas.project.backend.common.models.task.simple.TaskId
import com.khan366kos.atlas.project.backend.common.repo.IAtlasProjectTaskRepo
import com.khan366kos.atlas.project.backend.common.repo.IResourceRepo
import com.khan366kos.atlas.project.backend.calendar.service.CacheCalendarProvider
import com.khan366kos.atlas.project.backend.mappers.toUpdatableProjectDto
import com.khan366kos.atlas.project.backend.transport.resource.AssignmentDayOverrideListDto
import com.khan366kos.atlas.project.backend.transport.resource.CreateAssignmentCommandDto
import com.khan366kos.atlas.project.backend.transport.resource.SetDayOverrideCommandDto
import com.khan366kos.atlas.project.backend.transport.resource.TaskAssignmentListDto
import com.khan366kos.atlas.project.backend.transport.resource.UpdateAssignmentCommandDto
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.datetime.LocalDate

fun Route.assignments(
    taskRepo: IAtlasProjectTaskRepo,
    resourceRepo: IResourceRepo,
    calendarService: CacheCalendarProvider,
) {
    route("/assignments") {

        get {
            val projectId = call.parameters["projectId"]!!
            val plan = taskRepo.projectPlan(projectId)
            val assignments = resourceRepo.listAssignments(plan.id.asString())
            call.respond(TaskAssignmentListDto(assignments = assignments.map { it.toUpdatableProjectDto() }))
        }

        post {
            try {
                val projectId = call.parameters["projectId"]!!
                val request = call.receive<CreateAssignmentCommandDto>()
                val plan = taskRepo.projectPlan(projectId)
                val assignment = TaskAssignment(
                    taskId = TaskId(request.taskId),
                    resourceId = ResourceId(request.resourceId),
                    hoursPerDay = request.hoursPerDay,
                    plannedEffortHours = request.plannedEffortHours,
                )
                val created = resourceRepo.createAssignment(plan.id.asString(), assignment)
                call.respond(HttpStatusCode.Created, created.toUpdatableProjectDto())
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        patch("/{id}") {
            try {
                val projectId = call.parameters["projectId"]!!
                val id = call.parameters["id"]
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, "Assignment ID parameter is missing")

                val request = call.receive<UpdateAssignmentCommandDto>()
                val existing = resourceRepo.listAssignments(taskRepo.projectPlan(projectId).id.asString())
                    .find { it.id.value == id }
                    ?: return@patch call.respond(HttpStatusCode.NotFound)

                val updated = resourceRepo.updateAssignment(
                    id,
                    request.hoursPerDay ?: existing.hoursPerDay,
                    request.plannedEffortHours ?: existing.plannedEffortHours,
                )
                call.respond(updated.toUpdatableProjectDto())
            } catch (e: NoSuchElementException) {
                call.respond(HttpStatusCode.NotFound)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        delete("/{id}") {
            val id = call.parameters["id"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Assignment ID parameter is missing")

            resourceRepo.deleteAssignment(id)
            call.respond(HttpStatusCode.NoContent)
        }

        get("/{id}/day-overrides") {
            val id = call.parameters["id"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Assignment ID parameter is missing")

            val overrides = resourceRepo.getAssignmentDayOverrides(id)
            call.respond(AssignmentDayOverrideListDto(overrides = overrides.map { it.toUpdatableProjectDto() }))
        }

        post("/{id}/day-overrides") {
            try {
                val id = call.parameters["id"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, "Assignment ID parameter is missing")

                val request = call.receive<SetDayOverrideCommandDto>()
                val override = AssignmentDayOverride(
                    assignmentId = AssignmentId(id),
                    date = LocalDate.parse(request.date),
                    hours = request.hours,
                )
                resourceRepo.setAssignmentDayOverride(override)
                call.respond(HttpStatusCode.OK, override.toUpdatableProjectDto())
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        delete("/{id}/day-overrides/{date}") {
            val id = call.parameters["id"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Assignment ID parameter is missing")
            val dateStr = call.parameters["date"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Date parameter is missing")

            val date = LocalDate.parse(dateStr)
            resourceRepo.deleteAssignmentDayOverride(id, date)
            call.respond(HttpStatusCode.NoContent)
        }
    }

    route("/resource-load") {

        get {
            val projectId = call.parameters["projectId"]!!
            val fromStr = call.request.queryParameters["from"]
            val toStr = call.request.queryParameters["to"]
            if (fromStr == null || toStr == null) {
                return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "from and to query parameters are required"))
            }

            val from = LocalDate.parse(fromStr)
            val to = LocalDate.parse(toStr)
            val plan = taskRepo.projectPlan(projectId)
            val planId = plan.id.asString()
            val assignments = resourceRepo.listAssignments(planId)
            val resources = resourceRepo.listResources()
            val calendar = calendarService.current()
            val calendarOverrides = resources.associate { resource ->
                resource.id to resourceRepo.getCalendarOverrides(resource.id.value)
            }
            val allDayOverrides = resourceRepo.getAllDayOverridesForPlan(planId)
            val dayOverridesByAssignment = allDayOverrides.groupBy { it.assignmentId }

            val calculator = ResourceLoadCalculator(plan, assignments, resources, calendar, calendarOverrides, dayOverridesByAssignment)
            val report = calculator.computeLoad(from, to)
            call.respond(report.toUpdatableProjectDto())
        }

        get("/{resourceId}") {
            val projectId = call.parameters["projectId"]!!
            val resourceId = call.parameters["resourceId"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Resource ID parameter is missing")
            val fromStr = call.request.queryParameters["from"]
            val toStr = call.request.queryParameters["to"]
            if (fromStr == null || toStr == null) {
                return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "from and to query parameters are required"))
            }

            val from = LocalDate.parse(fromStr)
            val to = LocalDate.parse(toStr)
            val plan = taskRepo.projectPlan(projectId)
            val planId = plan.id.asString()
            val assignments = resourceRepo.listAssignments(planId)
            val resources = resourceRepo.listResources()
            val calendar = calendarService.current()
            val calendarOverrides = resources.associate { resource ->
                resource.id to resourceRepo.getCalendarOverrides(resource.id.value)
            }
            val allDayOverrides = resourceRepo.getAllDayOverridesForPlan(planId)
            val dayOverridesByAssignment = allDayOverrides.groupBy { it.assignmentId }

            val calculator = ResourceLoadCalculator(plan, assignments, resources, calendar, calendarOverrides, dayOverridesByAssignment)
            val result = calculator.computeResourceLoad(ResourceId(resourceId), from, to)
            call.respond(result.toUpdatableProjectDto())
        }
    }
}
