package com.khan366kos.atlas.project.backend.transport.commands

import com.khan366kos.atlas.project.backend.transport.portfolio.CreatablePortfolioDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class CreatePortfolioCommandDto(
    @SerialName("createPortfolio")
    val createPortfolio: CreatablePortfolioDto,
) {
}