package com.khan366kos.atlas.project.backend.transport.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchPortfolioResponseDto(
    @SerialName("messageType")
    val messageType: String,
    @SerialName("foundPortfolios")
    val foundPortfolios: List<PortfolioResponseDto>,
) {
    companion object {
        val NONE = SearchPortfolioResponseDto(messageType = "searchPortfolio", foundPortfolios = emptyList())
    }
}
