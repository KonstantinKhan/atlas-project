package com.khan366kos.atlas.project.backend.transport.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchPortfolioResponseDto(
    @SerialName("foundPortfolios")
    val foundPortfolios: List<ResponsePortfolioDto>? = null,
) {
    companion object {
        val NONE = SearchPortfolioResponseDto(emptyList())
    }
}

