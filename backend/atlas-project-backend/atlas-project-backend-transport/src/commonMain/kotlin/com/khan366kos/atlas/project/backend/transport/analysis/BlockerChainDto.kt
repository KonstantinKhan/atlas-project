package com.khan366kos.atlas.project.backend.transport.analysis

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class BlockerInfoDto(
    val taskId: String,
    val title: String,
    val status: String,
    val start: LocalDate? = null,
    val end: LocalDate? = null,
    val depth: Int,
)

@Serializable
data class BlockerChainDto(
    val targetTaskId: String,
    val blockers: List<BlockerInfoDto>,
)
