package com.khan366kos.atlas.project.backend.transport.commands

import com.khan366kos.atlas.project.backend.transport.enums.DependencyTypeDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateDependencyCommandDto(
    @SerialName("planId")
    val planId: String,
    @SerialName("fromTaskId")
    val fromTaskId: String,
    @SerialName("toTaskId")
    val toTaskId: String,
    @SerialName("type")
    val type: DependencyTypeDto = DependencyTypeDto.FS,
    @SerialName("lagDays")
    val lagDays: Int? = null
)
