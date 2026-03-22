package com.khan366kos.atlas.project.backend.ktor.app.routes

import com.khan366kos.atlas.project.backend.calendar.service.CacheCalendarProvider
import com.khan366kos.atlas.project.backend.common.models.projectPlan.availableTasks
import com.khan366kos.atlas.project.backend.common.models.projectPlan.blockerChain
import com.khan366kos.atlas.project.backend.common.models.projectPlan.whatIf
import com.khan366kos.atlas.project.backend.common.models.projectPlan.whatIfEnd
import com.khan366kos.atlas.project.backend.common.models.task.simple.TaskId
import com.khan366kos.atlas.project.backend.common.repo.IAtlasProjectTaskRepo
import com.khan366kos.atlas.project.backend.mappers.toDto
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import kotlinx.datetime.LocalDate

fun Route.analysis(
    repo: IAtlasProjectTaskRepo,
    calendarService: CacheCalendarProvider,
) = route("/analysis") {

    get("/blocker-chain/{taskId}") {
        val projectId = call.parameters["projectId"]!!
        val taskId = TaskId(call.parameters["taskId"]!!)
        val plan = repo.projectPlan(projectId)
        call.respond(plan.blockerChain(taskId).toDto())
    }

    get("/available-tasks") {
        val projectId = call.parameters["projectId"]!!
        val today = LocalDate.parse(call.request.queryParameters["today"]!!)
        val plan = repo.projectPlan(projectId)
        call.respond(plan.availableTasks(today).toDto())
    }

    get("/what-if") {
        val projectId = call.parameters["projectId"]!!
        val taskId = TaskId(call.request.queryParameters["taskId"]!!)
        val newStart = LocalDate.parse(call.request.queryParameters["newStart"]!!)
        val plan = repo.projectPlan(projectId)
        val calendar = calendarService.current()
        call.respond(plan.whatIf(taskId, newStart, calendar).toDto())
    }

    get("/what-if-end") {
        val projectId = call.parameters["projectId"]!!
        val taskId = TaskId(call.request.queryParameters["taskId"]!!)
        val newEnd = LocalDate.parse(call.request.queryParameters["newEnd"]!!)
        val plan = repo.projectPlan(projectId)
        val calendar = calendarService.current()
        call.respond(plan.whatIfEnd(taskId, newEnd, calendar).toDto())
    }
}
