package com.khan366kos.atlas.project.backend.transport.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeletePortfolioResponseDto(
    @SerialName("deletedPortfolio")
    val deletedPortfolio: ResponsePortfolioDto
)