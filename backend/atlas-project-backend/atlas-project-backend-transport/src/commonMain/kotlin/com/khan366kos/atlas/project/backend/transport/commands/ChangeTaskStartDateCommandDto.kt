package com.khan366kos.atlas.project.backend.transport.commands

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChangeTaskStartDateCommandDto(
    @SerialName("planId")
    val planId: String,
    @SerialName("taskId")
    val taskId: String,
    @SerialName("newPlannedStart")
    val newPlannedStart: LocalDate
)