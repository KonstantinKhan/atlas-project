package com.khan366kos.atlas.project.backend.transport.plan

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GantPlanDto(
    @SerialName("planId")
    val planId: String,
    @SerialName("projectId")
    val projectId: String,
)
