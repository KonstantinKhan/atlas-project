package com.khan366kos.atlas.project.backend.ktor.app.routes

import com.khan366kos.atlas.project.backend.common.models.resource.ResourceLevelingEngine
import com.khan366kos.atlas.project.backend.common.repo.IAtlasProjectTaskRepo
import com.khan366kos.atlas.project.backend.common.repo.IResourceRepo
import com.khan366kos.atlas.project.backend.calendar.service.CacheCalendarProvider
import com.khan366kos.atlas.project.backend.mappers.toUpdatableProjectDto
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.leveling(
    taskRepo: IAtlasProjectTaskRepo,
    resourceRepo: IResourceRepo,
    calendarService: CacheCalendarProvider,
) {
    route("/leveling") {

        post("/preview") {
            val projectId = call.parameters["projectId"]!!
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

            val engine = ResourceLevelingEngine(plan, assignments, resources, calendar, calendarOverrides, dayOverridesByAssignment)
            val result = engine.level()
            call.respond(HttpStatusCode.OK, result.toUpdatableProjectDto())
        }

        post("/apply") {
            val projectId = call.parameters["projectId"]!!
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

            val engine = ResourceLevelingEngine(plan, assignments, resources, calendar, calendarOverrides, dayOverridesByAssignment)
            val result = engine.level()

            for (schedule in result.scheduleDelta.updatedSchedule) {
                taskRepo.updateSchedule(schedule)
            }

            call.respond(HttpStatusCode.OK, result.toUpdatableProjectDto())
        }
    }
}
