package com.khan366kos.atlas.project.backend.transport.project

import com.khan366kos.atlas.project.backend.transport.enums.ProjectPriorityDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProjectDto(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("portfolio_id")
    val portfolioId: String,
    @SerialName("priority")
    val priority: ProjectPriorityDto
)
