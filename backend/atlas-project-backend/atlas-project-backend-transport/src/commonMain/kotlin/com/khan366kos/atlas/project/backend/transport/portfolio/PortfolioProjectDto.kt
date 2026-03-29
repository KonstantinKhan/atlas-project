package com.khan366kos.atlas.project.backend.transport.portfolio

import com.khan366kos.atlas.project.backend.transport.enums.ProjectPriorityDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PortfolioProjectDto(
    @SerialName("id")
    val id: String,
    @SerialName("portfolio_id")
    val portfolioId: String,
    @SerialName("project_id")
    val projectId: String,
    @SerialName("priority")
    val priority: ProjectPriorityDto,
)

@Serializable
data class PortfolioProjectListDto(
    @SerialName("portfolio_projects")
    val portfolioProjects: List<PortfolioProjectDto>,
)

@Serializable
data class AddProjectToPortfolioRequest(
    @SerialName("project_id")
    val projectId: String,
    @SerialName("priority")
    val priority: ProjectPriorityDto = ProjectPriorityDto.MEDIUM,
)

@Serializable
data class UpdateProjectPriorityRequest(
    @SerialName("priority")
    val priority: ProjectPriorityDto,
)

@Serializable
data class ReorderPortfolioProjectsRequest(
    @SerialName("project_ids")
    val projectIds: List<String>,
)
