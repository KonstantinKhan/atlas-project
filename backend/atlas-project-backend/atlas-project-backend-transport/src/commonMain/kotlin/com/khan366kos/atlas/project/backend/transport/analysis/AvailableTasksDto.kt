package com.khan366kos.atlas.project.backend.transport.analysis

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class AvailableTaskInfoDto(
    val taskId: String,
    val title: String,
    val status: String,
    val start: LocalDate,
    val end: LocalDate,
)

@Serializable
data class AvailableTasksDto(
    val tasks: List<AvailableTaskInfoDto>,
    val asOfDate: LocalDate,
)
