package com.khan366kos.atlas.project.backend.transport.cpm

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class CpmTaskDto(
    val taskId: String,
    val es: LocalDate,
    val ef: LocalDate,
    val ls: LocalDate,
    val lf: LocalDate,
    val slack: Int,
    val isCritical: Boolean,
)

@Serializable
data class CriticalPathDto(
    val tasks: List<CpmTaskDto>,
    val criticalTaskIds: List<String>,
    val projectEnd: LocalDate,
)
