package com.khan366kos.atlas.project.backend.transport.portfolio

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreatablePortfolioDto(
    @SerialName("name")
    val name: String? = null,
    @SerialName("description")
    val description: String? = null,
)
