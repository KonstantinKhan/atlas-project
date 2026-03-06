package com.khan366kos.atlas.project.backend.transport.commands

import com.khan366kos.atlas.project.backend.transport.enums.DependencyTypeDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChangeDependencyTypeCommandDto(
    @SerialName("fromTaskId")
    val fromTaskId: String,
    @SerialName("toTaskId")
    val toTaskId: String,
    @SerialName("newType")
    val newType: DependencyTypeDto,
)
