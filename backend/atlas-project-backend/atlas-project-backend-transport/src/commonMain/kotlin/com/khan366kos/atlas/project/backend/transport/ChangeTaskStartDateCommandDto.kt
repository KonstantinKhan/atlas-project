package com.khan366kos.atlas.project.backend.transport

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class ChangeTaskStartDateCommandDto(
    val planId: String,
    val taskId: String,
    val newPlannedStart: LocalDate
)
