package com.khan366kos.atlas.project.backend.ktor.app.routes

import com.khan366kos.atlas.project.backend.calendar.service.CacheCalendarProvider
import com.khan366kos.atlas.project.backend.common.models.portfolio.Portfolio
import com.khan366kos.atlas.project.backend.common.models.portfolio.PortfolioId
import com.khan366kos.atlas.project.backend.common.models.resource.CrossProjectLoadAggregator
import com.khan366kos.atlas.project.backend.common.models.resource.ProjectLoadInput
import com.khan366kos.atlas.project.backend.common.models.project.Project
import com.khan366kos.atlas.project.backend.common.project.ProjectPriority
import com.khan366kos.atlas.project.backend.common.repo.IAtlasProjectTaskRepo
import com.khan366kos.atlas.project.backend.common.repo.portfolio.IPortfolioRepo
import com.khan366kos.atlas.project.backend.common.repo.IResourceRepo
import com.khan366kos.atlas.project.backend.common.repo.portfolio.DbPortfolioIdRequest
import com.khan366kos.atlas.project.backend.mappers.toDomain
import com.khan366kos.atlas.project.backend.mappers.toResponsePortfolioDto
import com.khan366kos.atlas.project.backend.mappers.toUpdatableProjectDto
import com.khan366kos.atlas.project.backend.portfolio.service.PortfolioService
import com.khan366kos.atlas.project.backend.project.service.ProjectService
import com.khan366kos.atlas.project.backend.transport.commands.CreatePortfolioCommandDto
import com.khan366kos.atlas.project.backend.transport.commands.DeletePortfolioCommandDto
import com.khan366kos.atlas.project.backend.transport.commands.ReadPortfolioCommandDto
import com.khan366kos.atlas.project.backend.transport.commands.UpdatePortfolioCommandDto
import com.khan366kos.atlas.project.backend.transport.enums.ProjectPriorityDto
import com.khan366kos.atlas.project.backend.transport.portfolio.AddProjectToPortfolioRequest
import com.khan366kos.atlas.project.backend.transport.portfolio.ReorderPortfolioProjectsRequest
import com.khan366kos.atlas.project.backend.transport.portfolio.UpdateProjectPriorityRequest
import com.khan366kos.atlas.project.backend.transport.responses.CreatePortfolioResponseDto
import com.khan366kos.atlas.project.backend.transport.responses.DeletePortfolioResponseDto
import com.khan366kos.atlas.project.backend.transport.responses.ReadPortfolioResponseDto
import com.khan366kos.atlas.project.backend.transport.responses.SearchPortfolioResponseDto
import com.khan366kos.atlas.project.backend.transport.responses.UpdatePortfolioResponseDto
import com.khan366kos.com.khan366kos.atlas.project.backend.ktor.app.routes.projects
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

fun Routing.portfolios(
    portfolioRepo: IPortfolioRepo,
    taskRepo: IAtlasProjectTaskRepo,
    resourceRepo: IResourceRepo,
    calendarService: CacheCalendarProvider,
    portfolioService: PortfolioService,
    projectService: ProjectService,
) = route("/portfolios") {

    get {
        val portfolios = portfolioService.list()
        call.respond(
            SearchPortfolioResponseDto(
                messageType = "searchPortfolio",
                foundPortfolios = portfolios.map { it.toResponsePortfolioDto() },
            )
        )
    }

    post {
        val command = call.receive<CreatePortfolioCommandDto>()
        val portfolio = portfolioService.create(command.createPortfolio.toDomain())
        call.respond(
            HttpStatusCode.Created, CreatePortfolioResponseDto(
                messageType = "createPortfolio",
                createdPortfolio = portfolio.toResponsePortfolioDto(),
            )
        )
    }

    get("/{id}") {
        val id = call.parameters["id"]!!
        val portfolio =
            portfolioService.find(PortfolioId(id))
        call.respond(ReadPortfolioResponseDto(messageType = "readPortfolio", readPortfolio = portfolio.toResponsePortfolioDto()))
    }

    patch("/{id}") {
        val command = call.receive<UpdatePortfolioCommandDto>()
        val portfolio = portfolioService.modify(command.updatePortfolio.toDomain())
        call.respond(HttpStatusCode.OK, UpdatePortfolioResponseDto(messageType = "updatePortfolio", updatedPortfolio = portfolio.toResponsePortfolioDto()))
    }

    delete("/{id}") {
        val id = call.parameters["id"]!!
        val portfolio = portfolioService.delete(PortfolioId(id))
        call.respond(DeletePortfolioResponseDto(messageType = "deletePortfolio", deletedPortfolio = portfolio.toResponsePortfolioDto()))
    }

    // Portfolio-Project relationship endpoints
//    get("/{id}/projects") {
//        val id = call.parameters["id"]!!
//        portfolioRepo.readPortfolio(DbPortfolioIdRequest(PortfolioId(id)))
//            ?: return@get call.respond(HttpStatusCode.NotFound)
//        val portfolioProjects = portfolioRepo.listPortfolioProjects(id)
//
//        // Load project details and task counts for each portfolio project
//        val projectSummaries = portfolioProjects.map { portfolioProject ->
//            val projectId = portfolioProject.id.asString()
//            val project = portfolioRepo.getProject(projectId)
//            val plan = taskRepo.projectPlan(projectId)
//            val taskCount = taskRepo.countTasks(plan.id.asString())
//
//            ProjectSummaryDto(
//                id = projectId,
//                name = project?.name?.asString() ?: "Unknown",
//                priority = ProjectPriorityDto.MEDIUM,
//                taskCount = taskCount,
//            )
//        }
//
//        call.respond(ProjectSummaryListDto(projects = projectSummaries))
//    }

    post("/{id}/projects") {
        val id = call.parameters["id"]!!
        portfolioRepo.readPortfolio(DbPortfolioIdRequest(PortfolioId(id)))
            ?: return@post call.respond(HttpStatusCode.NotFound)
        val request = call.receive<AddProjectToPortfolioRequest>()
        val portfolioProject = portfolioRepo.addProjectToPortfolio(
            id,
            request.projectId,
            request.priority.toDomain()
        )
        call.respond(HttpStatusCode.Created, portfolioProject.toResponsePortfolioDto())
    }

    delete("/{id}/projects/{projectId}") {
        val portfolioId = call.parameters["id"]!!
        val projectId = call.parameters["projectId"]!!
        portfolioRepo.readPortfolio(DbPortfolioIdRequest(PortfolioId(projectId)))
            ?: return@delete call.respond(HttpStatusCode.NotFound)
        portfolioRepo.removeProjectFromPortfolio(portfolioId, projectId)
        call.respond(HttpStatusCode.NoContent)
    }

    patch("/{id}/projects/{projectId}/priority") {
        val portfolioId = call.parameters["id"]!!
        val projectId = call.parameters["projectId"]!!
        portfolioRepo.readPortfolio(DbPortfolioIdRequest(PortfolioId(projectId)))
            ?: return@patch call.respond(HttpStatusCode.NotFound)
        val request = call.receive<UpdateProjectPriorityRequest>()
        portfolioRepo.updateProjectPriority(portfolioId, projectId, request.priority.toDomain())
        call.respond(HttpStatusCode.OK)
    }

    patch("/{id}/projects/reorder") {
        val id = call.parameters["id"]!!
        portfolioRepo.readPortfolio(DbPortfolioIdRequest(PortfolioId(id)))
            ?: return@patch call.respond(HttpStatusCode.NotFound)
        val request = call.receive<ReorderPortfolioProjectsRequest>()
        portfolioRepo.reorderPortfolioProjects(id, request.projectIds)
        call.respond(HttpStatusCode.OK)
    }

    get("/{id}/resource-load") {
        val id = call.parameters["id"]!!
        portfolioRepo.readPortfolio(DbPortfolioIdRequest(PortfolioId(id)))
            ?: return@get call.respond(HttpStatusCode.NotFound)

        // Load projects from this portfolio + all other projects (for external load)
        val allProjects = portfolioRepo.listAllProjects()
        val portfolioProjects = portfolioRepo.listPortfolioProjects(id)
        val portfolioProjectIds = portfolioProjects.map { it.id.asString() }.toSet()

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
                priority = ProjectPriority.MEDIUM,
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
        call.respond(report.toUpdatableProjectDto())
    }
}

private fun Portfolio.toDto() = PortfolioDto(
    id = id.asString(),
    name = name.value,
    description = description.value,
)

private fun Project.toSummaryDto(taskCount: Int) = ProjectSummaryDto(
    id = id.asString(),
    name = name.asString(),
    priority = ProjectPriorityDto.MEDIUM,
    taskCount = taskCount,
)
