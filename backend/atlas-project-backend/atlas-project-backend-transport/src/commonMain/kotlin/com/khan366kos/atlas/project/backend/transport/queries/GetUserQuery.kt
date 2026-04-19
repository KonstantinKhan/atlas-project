package com.khan366kos.atlas.project.backend.transport.queries

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetUserQuery(
    @SerialName("getUserId")
    val getUserId: String,
)
