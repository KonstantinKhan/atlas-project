package com.khan366kos.atlas.project.backend.transport.responses.project

import com.khan366kos.atlas.project.backend.transport.project.ProjectDto
import kotlinx.serialization.Serializable

@Serializable
class SearchProjectResponseDto(
    val projects: List<ProjectResponseDto>,
) {
}