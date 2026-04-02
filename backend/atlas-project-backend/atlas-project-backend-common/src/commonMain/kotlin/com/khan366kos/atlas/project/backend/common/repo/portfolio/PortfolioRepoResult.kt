package com.khan366kos.atlas.project.backend.common.repo.portfolio

import com.khan366kos.atlas.project.backend.common.models.portfolio.Portfolio

sealed class PortfolioRepoResult {
    data class Single(val portfolio: Portfolio) : PortfolioRepoResult()
    data class Multiple(val portfolios: List<Portfolio>) : PortfolioRepoResult()
    data object NotFound: PortfolioRepoResult()
    data class DbError(val cause: Throwable) : PortfolioRepoResult()
}