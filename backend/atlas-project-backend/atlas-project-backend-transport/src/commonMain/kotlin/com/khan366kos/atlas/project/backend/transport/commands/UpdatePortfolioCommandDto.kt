package com.khan366kos.atlas.project.backend.transport.commands

import com.khan366kos.atlas.project.backend.transport.portfolio.UpdatablePortfolioDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class UpdatePortfolioCommandDto(
    @SerialName("updatePortfolio")
    val updatePortfolio: UpdatablePortfolioDto,
)
