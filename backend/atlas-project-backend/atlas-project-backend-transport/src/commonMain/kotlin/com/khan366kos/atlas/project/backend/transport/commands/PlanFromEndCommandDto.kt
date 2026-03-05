package com.khan366kos.atlas.project.backend.transport.commands

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlanFromEndCommandDto(
    @SerialName("taskId")
    val taskId: String,
    @SerialName("newPlannedEnd")
    val newPlannedEnd: String
)
