package com.khan366kos.atlas.project.backend.transport.responses

import com.khan366kos.atlas.project.backend.transport.portfolio.UpdatablePortfolioDto
import kotlinx.serialization.Serializable

@Serializable
data class SearchPortfoliosResponseDto(
    val portfolios: List<UpdatablePortfolioDto>
) {
    companion object {
        val NONE = SearchPortfoliosResponseDto(emptyList())
    }
}

