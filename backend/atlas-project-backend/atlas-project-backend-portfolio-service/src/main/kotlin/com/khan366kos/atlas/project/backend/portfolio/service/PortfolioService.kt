package com.khan366kos.atlas.project.backend.portfolio.service

import com.khan366kos.atlas.project.backend.common.exceptions.PortfolioNotFoundException
import com.khan366kos.atlas.project.backend.common.exceptions.PortfolioOperationFailedException
import com.khan366kos.atlas.project.backend.common.models.portfolio.Portfolio
import com.khan366kos.atlas.project.backend.common.models.portfolio.PortfolioId
import com.khan366kos.atlas.project.backend.common.repo.IPortfolioRepo
import com.khan366kos.atlas.project.backend.common.repo.portfolio.DbPortfolioIdRequest
import com.khan366kos.atlas.project.backend.common.repo.portfolio.DbPortfolioRequest
import com.khan366kos.atlas.project.backend.common.repo.portfolio.PortfolioRepoResult

class PortfolioService(
    private val repo: IPortfolioRepo
) {

    suspend fun create(portfolio: Portfolio): Portfolio {
        return when (val result = repo.createPortfolio(DbPortfolioRequest(portfolio))) {
            is PortfolioRepoResult.Single -> result.portfolio
            is PortfolioRepoResult.DbError -> throw PortfolioOperationFailedException(result.cause)
            else -> throw PortfolioOperationFailedException(RuntimeException("Unexpected result"))
        }
    }

    suspend fun find(id: PortfolioId): Portfolio {
        return when (val result = repo.readPortfolio(DbPortfolioIdRequest(id))) {
            is PortfolioRepoResult.Single -> result.portfolio
            is PortfolioRepoResult.NotFound -> throw PortfolioNotFoundException(id.asString())
            is PortfolioRepoResult.DbError -> throw PortfolioOperationFailedException(result.cause)
            else -> throw PortfolioOperationFailedException(RuntimeException("Unexpected result"))
        }
    }

    suspend fun modify(portfolio: Portfolio): Portfolio {
        return when (val result = repo.updatePortfolio(DbPortfolioRequest(portfolio))) {
            is PortfolioRepoResult.Single -> result.portfolio
            is PortfolioRepoResult.NotFound -> throw PortfolioNotFoundException(portfolio.id.asString())
            is PortfolioRepoResult.DbError -> throw PortfolioOperationFailedException(result.cause)
            else -> throw PortfolioOperationFailedException(RuntimeException("Unexpected result"))
        }
    }

    suspend fun delete(id: PortfolioId): Portfolio {
        return when (val result = repo.deletePortfolio(DbPortfolioIdRequest(id))) {
            is PortfolioRepoResult.Single -> result.portfolio
            is PortfolioRepoResult.NotFound -> throw PortfolioNotFoundException(id.asString())
            is PortfolioRepoResult.DbError -> throw PortfolioOperationFailedException(result.cause)
            else -> throw PortfolioOperationFailedException(RuntimeException("Unexpected result"))
        }
    }

    suspend fun list(): List<Portfolio> {
        return when (val result = repo.searchPortfolio()) {
            is PortfolioRepoResult.Multiple -> result.portfolios
            is PortfolioRepoResult.DbError -> throw PortfolioOperationFailedException(result.cause)
            else -> emptyList()
        }
    }
}