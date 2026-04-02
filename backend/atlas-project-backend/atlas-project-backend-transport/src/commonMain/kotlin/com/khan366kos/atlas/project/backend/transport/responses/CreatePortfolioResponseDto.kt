package com.khan366kos.atlas.project.backend.transport.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class CreatePortfolioResponseDto(
    @SerialName("createdPortfolio")
    val createdPortfolio: PortfolioResponseDto,
)