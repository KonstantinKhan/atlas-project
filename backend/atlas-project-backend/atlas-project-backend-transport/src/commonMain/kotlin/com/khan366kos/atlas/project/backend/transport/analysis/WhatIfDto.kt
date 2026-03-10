package com.khan366kos.atlas.project.backend.transport.analysis

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class TaskImpactDto(
    val taskId: String,
    val title: String,
    val oldStart: LocalDate,
    val oldEnd: LocalDate,
    val newStart: LocalDate,
    val newEnd: LocalDate,
    val deltaStartDays: Int,
    val deltaEndDays: Int,
)

@Serializable
data class WhatIfDto(
    val movedTaskId: String,
    val impacts: List<TaskImpactDto>,
)
