package com.khan366kos.atlas.project.backend.common.models.portfolio

data class Portfolio(
    val id: PortfolioId,
    val name: String,
    val description: String,
) {
    companion object {
        val NONE = Portfolio(
            id = PortfolioId.NONE,
            name = "",
            description = "",
        )
    }
}
