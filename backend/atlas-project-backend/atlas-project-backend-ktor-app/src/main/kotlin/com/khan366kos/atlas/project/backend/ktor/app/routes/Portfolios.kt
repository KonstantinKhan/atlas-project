package com.khan366kos.atlas.project.backend.ktor.app.routes

import com.khan366kos.atlas.project.backend.calendar.service.CacheCalendarProvider
import com.khan366kos.atlas.project.backend.common.models.portfolio.Portfolio
import com.khan366kos.atlas.project.backend.common.models.resource.CrossProjectLoadAggregator
import com.khan366kos.atlas.project.backend.common.models.resource.ProjectLoadInput
import com.khan366kos.atlas.project.backend.common.project.Project
import com.khan366kos.atlas.project.backend.common.repo.IAtlasProjectTaskRepo
import com.khan366kos.atlas.project.backend.common.repo.IPortfolioRepo
import com.khan366kos.atlas.project.backend.common.repo.IResourceRepo
import com.khan366kos.atlas.project.backend.mappers.toDto
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable

@Serializable
data class PortfolioDto(
    val id: String,
    val name: String,
    val description: String,
)

@Serializable
data class PortfolioListDto(
    val portfolios: List<PortfolioDto>,
)

@Serializable
data class CreatePortfolioRequest(
    val name: String,
    val description: String = "",
)

@Serializable
data class UpdatePortfolioRequest(
    val name: String? = null,
    val description: String? = null,
)

@Serializable
data class ProjectSummaryDto(
    val id: String,
    val name: String,
    val priority: Int,
    val taskCount: Int,
)

@Serializable
data class ProjectSummaryListDto(
    val projects: List<ProjectSummaryDto>,
)

@Serializable
data class CreateProjectRequest(
    val name: String,
    val priority: Int = 0,
)

@Serializable
data class ReorderProjectsRequest(
    val projectPriorities: List<ProjectPriorityEntry>,
)

@Serializable
data class ProjectPriorityEntry(
    val projectId: String,
    val priority: Int,
)

fun Routing.portfolios(
    portfolioRepo: IPortfolioRepo,
    taskRepo: IAtlasProjectTaskRepo,
    resourceRepo: IResourceRepo,
    calendarService: CacheCalendarProvider,
) = route("/portfolios") {

    get {
        val portfolios = portfolioRepo.listPortfolios()
        call.respond(PortfolioListDto(portfolios = portfolios.map { it.toDto() }))
    }

    post {
        val request = call.receive<CreatePortfolioRequest>()
        val created = portfolioRepo.createPortfolio(
            Portfolio(name = request.name, description = request.description)
        )
        call.respond(HttpStatusCode.Created, created.toDto())
    }

    get("/{id}") {
        val id = call.parameters["id"]!!
        val portfolio = portfolioRepo.getPortfolio(id)
            ?: return@get call.respond(HttpStatusCode.NotFound)
        call.respond(portfolio.toDto())
    }

    patch("/{id}") {
        val id = call.parameters["id"]!!
        val existing = portfolioRepo.getPortfolio(id)
            ?: return@patch call.respond(HttpStatusCode.NotFound)
        val request = call.receive<UpdatePortfolioRequest>()
        val updated = existing.copy(
            name = request.name ?: existing.name,
            description = request.description ?: existing.description,
        )
        call.respond(portfolioRepo.updatePortfolio(updated).toDto())
    }

    delete("/{id}") {
        val id = call.parameters["id"]!!
        portfolioRepo.getPortfolio(id)
            ?: return@delete call.respond(HttpStatusCode.NotFound)
        portfolioRepo.deletePortfolio(id)
        call.respond(HttpStatusCode.NoContent)
    }

    get("/{id}/projects") {
        val id = call.parameters["id"]!!
        portfolioRepo.getPortfolio(id)
            ?: return@get call.respond(HttpStatusCode.NotFound)
        val projects = portfolioRepo.listProjects(id)
        val summaries = projects.map { project ->
            val taskCount = taskRepo.countTasks(project.id.asString())
            project.toSummaryDto(taskCount)
        }
        call.respond(ProjectSummaryListDto(projects = summaries))
    }

    post("/{id}/projects") {
        val id = call.parameters["id"]!!
        portfolioRepo.getPortfolio(id)
            ?: return@post call.respond(HttpStatusCode.NotFound)
        val request = call.receive<CreateProjectRequest>()
        val project = portfolioRepo.createProject(id, request.name, request.priority)
        call.respond(HttpStatusCode.Created, project.toSummaryDto(taskCount = 0))
    }

    patch("/{id}/projects/reorder") {
        val id = call.parameters["id"]!!
        portfolioRepo.getPortfolio(id)
            ?: return@patch call.respond(HttpStatusCode.NotFound)
        val request = call.receive<ReorderProjectsRequest>()
        for (entry in request.projectPriorities) {
            portfolioRepo.updateProject(entry.projectId, name = null, priority = entry.priority)
        }
        call.respond(HttpStatusCode.OK)
    }

    get("/{id}/resource-load") {
        val id = call.parameters["id"]!!
        portfolioRepo.getPortfolio(id)
            ?: return@get call.respond(HttpStatusCode.NotFound)

        // Load projects from this portfolio + all other projects (for external load)
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

private fun Portfolio.toDto() = PortfolioDto(
    id = id.value,
    name = name,
    description = description,
)

private fun Project.toSummaryDto(taskCount: Int) = ProjectSummaryDto(
    id = id.asString(),
    name = name.asString(),
    priority = priority,
    taskCount = taskCount,
)
