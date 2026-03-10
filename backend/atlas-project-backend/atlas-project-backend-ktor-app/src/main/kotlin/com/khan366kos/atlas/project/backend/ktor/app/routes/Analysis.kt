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
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import kotlinx.datetime.LocalDate

fun Routing.analysis(
    repo: IAtlasProjectTaskRepo,
    calendarService: CacheCalendarProvider,
) = route("/analysis") {

    get("/blocker-chain/{taskId}") {
        val taskId = TaskId(call.parameters["taskId"]!!)
        val plan = repo.projectPlan()
        call.respond(plan.blockerChain(taskId).toDto())
    }

    get("/available-tasks") {
        val today = LocalDate.parse(call.request.queryParameters["today"]!!)
        val plan = repo.projectPlan()
        call.respond(plan.availableTasks(today).toDto())
    }

    get("/what-if") {
        val taskId = TaskId(call.request.queryParameters["taskId"]!!)
        val newStart = LocalDate.parse(call.request.queryParameters["newStart"]!!)
        val plan = repo.projectPlan()
        val calendar = calendarService.current()
        call.respond(plan.whatIf(taskId, newStart, calendar).toDto())
    }

    get("/what-if-end") {
        val taskId = TaskId(call.request.queryParameters["taskId"]!!)
        val newEnd = LocalDate.parse(call.request.queryParameters["newEnd"]!!)
        val plan = repo.projectPlan()
        val calendar = calendarService.current()
        call.respond(plan.whatIfEnd(taskId, newEnd, calendar).toDto())
    }
}
