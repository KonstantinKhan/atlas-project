package com.khan366kos.atlas.project.backend.transport.responses.project

import com.khan366kos.atlas.project.backend.transport.enums.ProjectPriorityDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProjectResponseDto(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("priority")
    val priority: ProjectPriorityDto,
) {
}