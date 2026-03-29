package com.khan366kos.atlas.project.backend.transport.project

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProjectDto(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
)
