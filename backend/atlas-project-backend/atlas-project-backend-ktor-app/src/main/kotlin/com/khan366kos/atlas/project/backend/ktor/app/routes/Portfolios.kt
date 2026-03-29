package com.khan366kos.atlas.project.backend.ktor.app.routes

import com.khan366kos.atlas.project.backend.calendar.service.CacheCalendarProvider
import com.khan366kos.atlas.project.backend.common.models.portfolio.Portfolio
import com.khan366kos.atlas.project.backend.common.models.resource.CrossProjectLoadAggregator
import com.khan366kos.atlas.project.backend.common.models.resource.ProjectLoadInput
import com.khan366kos.atlas.project.backend.common.project.Project
import com.khan366kos.atlas.project.backend.common.project.ProjectPriority
import com.khan366kos.atlas.project.backend.common.repo.IAtlasProjectTaskRepo
import com.khan366kos.atlas.project.backend.common.repo.IPortfolioRepo
import com.khan366kos.atlas.project.backend.common.repo.IResourceRepo
import com.khan366kos.atlas.project.backend.mappers.toDomain
import com.khan366kos.atlas.project.backend.mappers.toDto
import com.khan366kos.atlas.project.backend.transport.enums.ProjectPriorityDto
import com.khan366kos.atlas.project.backend.transport.portfolio.AddProjectToPortfolioRequest
import com.khan366kos.atlas.project.backend.transport.portfolio.PortfolioProjectListDto
import com.khan366kos.atlas.project.backend.transport.portfolio.ReorderPortfolioProjectsRequest
import com.khan366kos.atlas.project.backend.transport.portfolio.UpdateProjectPriorityRequest
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
    val priority: ProjectPriorityDto,
    val taskCount: Int,
)

@Serializable
data class ProjectSummaryListDto(
    val projects: List<ProjectSummaryDto>,
)

@Serializable
data class CreateProjectRequest(
    val name: String,
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

    // Portfolio-Project relationship endpoints
    get("/{id}/projects") {
        val id = call.parameters["id"]!!
        portfolioRepo.getPortfolio(id)
            ?: return@get call.respond(HttpStatusCode.NotFound)
        val portfolioProjects = portfolioRepo.listPortfolioProjects(id)
        
        // Load project details and task counts for each portfolio project
        val projectSummaries = portfolioProjects.map { portfolioProject ->
            val projectId = portfolioProject.projectId.asString()
            val project = portfolioRepo.getProject(projectId)
            val plan = taskRepo.projectPlan(projectId)
            val taskCount = taskRepo.countTasks(plan.id.asString())
            
            ProjectSummaryDto(
                id = projectId,
                name = project?.name?.asString() ?: "Unknown",
                priority = portfolioProject.priority.toDto(),
                taskCount = taskCount,
            )
        }
        
        call.respond(ProjectSummaryListDto(projects = projectSummaries))
    }

    post("/{id}/projects") {
        val id = call.parameters["id"]!!
        portfolioRepo.getPortfolio(id)
            ?: return@post call.respond(HttpStatusCode.NotFound)
        val request = call.receive<AddProjectToPortfolioRequest>()
        val portfolioProject = portfolioRepo.addProjectToPortfolio(
            id,
            request.projectId,
            request.priority.toDomain()
        )
        call.respond(HttpStatusCode.Created, portfolioProject.toDto())
    }

    delete("/{id}/projects/{projectId}") {
        val portfolioId = call.parameters["id"]!!
        val projectId = call.parameters["projectId"]!!
        portfolioRepo.getPortfolio(portfolioId)
            ?: return@delete call.respond(HttpStatusCode.NotFound)
        portfolioRepo.removeProjectFromPortfolio(portfolioId, projectId)
        call.respond(HttpStatusCode.NoContent)
    }

    patch("/{id}/projects/{projectId}/priority") {
        val portfolioId = call.parameters["id"]!!
        val projectId = call.parameters["projectId"]!!
        portfolioRepo.getPortfolio(portfolioId)
            ?: return@patch call.respond(HttpStatusCode.NotFound)
        val request = call.receive<UpdateProjectPriorityRequest>()
        portfolioRepo.updateProjectPriority(portfolioId, projectId, request.priority.toDomain())
        call.respond(HttpStatusCode.OK)
    }

    patch("/{id}/projects/reorder") {
        val id = call.parameters["id"]!!
        portfolioRepo.getPortfolio(id)
            ?: return@patch call.respond(HttpStatusCode.NotFound)
        val request = call.receive<ReorderPortfolioProjectsRequest>()
        portfolioRepo.reorderPortfolioProjects(id, request.projectIds)
        call.respond(HttpStatusCode.OK)
    }

    get("/{id}/resource-load") {
        val id = call.parameters["id"]!!
        portfolioRepo.getPortfolio(id)
            ?: return@get call.respond(HttpStatusCode.NotFound)

        // Load projects from this portfolio + all other projects (for external load)
        val allProjects = portfolioRepo.listAllProjects()
        val portfolioProjects = portfolioRepo.listPortfolioProjects(id)
        val portfolioProjectIds = portfolioProjects.map { it.projectId.asString() }.toSet()

        val projectInputs = allProjects.map { project ->
            val projectId = project.id.asString()
            val plan = taskRepo.projectPlan(projectId)
            val assignments = resourceRepo.listAssignments(projectId)
            val dayOverrides = resourceRepo.getAllDayOverridesForPlan(projectId).groupBy { it.assignmentId }
            val isInPortfolio = projectId in portfolioProjectIds
            ProjectLoadInput(
                projectId = projectId,
                projectName = project.name.asString(),
                portfolioId = if (isInPortfolio) id else "",
                priority = if (isInPortfolio) {
                    portfolioProjects.find { it.projectId.asString() == projectId }?.priority ?: ProjectPriority.MEDIUM
                } else {
                    ProjectPriority.MEDIUM
                },
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
    priority = ProjectPriorityDto.MEDIUM,
    taskCount = taskCount,
)
