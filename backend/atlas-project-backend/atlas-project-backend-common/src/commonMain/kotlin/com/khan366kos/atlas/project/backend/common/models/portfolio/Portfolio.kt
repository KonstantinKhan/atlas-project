package com.khan366kos.atlas.project.backend.common.models.portfolio

data class Portfolio(
    val id: PortfolioId,
    val name: PortfolioName,
    val description: PortfolioDescription,
) {
    companion object {
        val NONE = Portfolio(
            id = PortfolioId.NONE,
            name = PortfolioName.NONE,
            description = PortfolioDescription.NONE,
        )
    }
}
