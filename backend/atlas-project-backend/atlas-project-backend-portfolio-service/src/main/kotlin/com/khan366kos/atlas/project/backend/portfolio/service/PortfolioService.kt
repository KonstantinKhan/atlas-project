package com.khan366kos.atlas.project.backend.porfolio.service

import com.khan366kos.atlas.project.backend.common.models.portfolio.Portfolio
import com.khan366kos.atlas.project.backend.common.repo.IPortfolioRepo
import com.khan366kos.atlas.project.backend.common.repo.portfolio.DbPortfolioFilterRequest
import com.khan366kos.atlas.project.backend.common.repo.portfolio.DbPortfoliosResponse

class PortfolioService(
    private val repo: IPortfolioRepo
) {
    suspend fun portfolios(): List<Portfolio> {
        val response = repo.listPortfolios()
        return if (response.isSuccess) {
            response.result
        } else emptyList()
    }
}