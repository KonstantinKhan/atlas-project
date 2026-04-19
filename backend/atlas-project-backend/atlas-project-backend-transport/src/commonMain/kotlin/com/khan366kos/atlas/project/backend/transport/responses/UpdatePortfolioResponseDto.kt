package com.khan366kos.atlas.project.backend.transport.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdatePortfolioResponseDto(
    @SerialName("messageType")
    val messageType: String,
    @SerialName("updatedPortfolio")
    val updatedPortfolio: PortfolioResponseDto,
)
