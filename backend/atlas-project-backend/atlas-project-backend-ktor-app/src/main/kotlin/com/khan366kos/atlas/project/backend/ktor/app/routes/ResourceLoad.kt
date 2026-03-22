package com.khan366kos.atlas.project.backend.ktor.app.routes

import com.khan366kos.atlas.project.backend.calendar.service.CacheCalendarProvider
import com.khan366kos.atlas.project.backend.common.models.ProjectDate
import com.khan366kos.atlas.project.backend.common.models.resource.CrossProjectLoadAggregator
import com.khan366kos.atlas.project.backend.common.models.resource.ProjectLoadInput
import com.khan366kos.atlas.project.backend.common.models.projectPlan.ProjectPlan
import com.khan366kos.atlas.project.backend.common.models.taskSchedule.TaskScheduleId
import com.khan366kos.atlas.project.backend.common.repo.IAtlasProjectTaskRepo
import com.khan366kos.atlas.project.backend.common.repo.IPortfolioRepo
import com.khan366kos.atlas.project.backend.common.repo.IResourceRepo
import com.khan366kos.atlas.project.backend.mappers.toDto
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

fun Routing.resourceLoad(
    portfolioRepo: IPortfolioRepo,
    taskRepo: IAtlasProjectTaskRepo,
    resourceRepo: IResourceRepo,
    calendarService: CacheCalendarProvider,
) = route("/resource-load") {

    get {
        val allProjects = portfolioRepo.listAllProjects()

        val projectInputs = allProjects.map { project ->
            val projectId = project.id.asString()
            val plan = taskRepo.projectPlan(projectId)
            val assignments = resourceRepo.listAssignments(projectId)
            val dayOverrides = resourceRepo.getAllDayOverridesForPlan(projectId).groupBy { it.assignmentId }
            ProjectLoadInput(
                projectId = projectId,
                projectName = project.name.asString(),
                portfolioId = project.portfolioId.asString(),
                priority = project.priority,
                plan = plan,
                assignments = assignments,
                dayOverrides = dayOverrides,
            )
        }

        val resources = resourceRepo.listResources()
        val calendar = calendarService.current()
        val calendarOverrides = resources.associate { resource ->
            resource.id to resourceRepo.getCalendarOverrides(resource.id.value)
        }

        val (from, to) = parseDateRange(call.request.queryParameters, projectInputs.map { it.plan })

        val aggregator = CrossProjectLoadAggregator(projectInputs, resources, calendar, calendarOverrides)
        val report = aggregator.computeLoad(from, to)
        call.respond(report.toDto())
    }
}

internal fun parseDateRange(
    queryParameters: io.ktor.http.Parameters,
    plans: List<ProjectPlan>,
): Pair<LocalDate, LocalDate> {
    val fromStr = queryParameters["from"]
    val toStr = queryParameters["to"]
    if (fromStr != null && toStr != null) {
        return LocalDate.parse(fromStr) to LocalDate.parse(toStr)
    }
    // Auto-compute from min/max dates of all scheduled tasks
    var minDate: LocalDate? = null
    var maxDate: LocalDate? = null
    for (plan in plans) {
        for (schedule in plan.schedules().values) {
            val start = (schedule.start as? ProjectDate.Set)?.date
            val end = (schedule.end as? ProjectDate.Set)?.date
            if (start != null && (minDate == null || start < minDate)) minDate = start
            if (end != null && (maxDate == null || end > maxDate)) maxDate = end
        }
    }
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    return (minDate ?: today) to (maxDate ?: today)
}
