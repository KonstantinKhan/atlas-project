package com.khan366kos.atlas.project.backend.portfolio.service

import com.khan366kos.atlas.project.backend.common.models.portfolio.PortfolioId
import com.khan366kos.atlas.project.backend.common.repo.IPortfolioRepo
import com.khan366kos.atlas.project.backend.common.repo.portfolio.DbPortfolioIdRequest
import com.khan366kos.atlas.project.backend.common.repo.portfolio.DbPortfolioRequest
import com.khan366kos.atlas.project.backend.mappers.toDomain
import com.khan366kos.atlas.project.backend.mappers.toResponsePortfolioDto
import com.khan366kos.atlas.project.backend.transport.commands.CreatePortfolioCommandDto
import com.khan366kos.atlas.project.backend.transport.commands.DeletePortfolioCommandDto
import com.khan366kos.atlas.project.backend.transport.commands.ReadPortfolioCommandDto
import com.khan366kos.atlas.project.backend.transport.commands.UpdatePortfolioCommandDto
import com.khan366kos.atlas.project.backend.transport.responses.CreatePortfolioResponseDto
import com.khan366kos.atlas.project.backend.transport.responses.DeletePortfolioResponseDto
import com.khan366kos.atlas.project.backend.transport.responses.ReadPortfolioResponseDto
import com.khan366kos.atlas.project.backend.transport.responses.SearchPortfolioResponseDto
import com.khan366kos.atlas.project.backend.transport.responses.UpdatePortfolioResponseDto

class PortfolioService(
    private val repo: IPortfolioRepo
) {

    suspend fun create(createPortfolioCommandDto: CreatePortfolioCommandDto): CreatePortfolioResponseDto {
        val portfolio = createPortfolioCommandDto.createPortfolio.toDomain()
        val dbResponse = repo.createPortfolio(DbPortfolioRequest(portfolio))
        return CreatePortfolioResponseDto(
            createdPortfolio = dbResponse.result.toResponsePortfolioDto()
        )
    }

    suspend fun find(readPortfolioCommandDto: ReadPortfolioCommandDto): ReadPortfolioResponseDto {
        val portfolioId = readPortfolioCommandDto.readPortfolioId?.let { PortfolioId(it) } ?: PortfolioId.NONE
        val dbResponse = repo.readPortfolio(DbPortfolioIdRequest(portfolioId))
        return ReadPortfolioResponseDto(
            readPortfolio = dbResponse.result.toResponsePortfolioDto()
        )
    }

    suspend fun modify(updatePortfolioCommandDto: UpdatePortfolioCommandDto): UpdatePortfolioResponseDto {
        val portfolio = updatePortfolioCommandDto.updatePortfolio.toDomain()
        val dbResponse = repo.updatePortfolio(DbPortfolioRequest(portfolio))
        return UpdatePortfolioResponseDto(
            updatedPortfolio = dbResponse.result.toResponsePortfolioDto()
        )
    }

    suspend fun delete(deletePortfolioCommandDto: DeletePortfolioCommandDto): DeletePortfolioResponseDto {
        val dbResponse =
            repo.deletePortfolio(
                DbPortfolioIdRequest(
                    deletePortfolioCommandDto.deletePortfolioId
                        ?.let { PortfolioId(it) }
                        ?: PortfolioId.NONE))
        return DeletePortfolioResponseDto(
            dbResponse.result.toResponsePortfolioDto()
        )
    }

    suspend fun list(): SearchPortfolioResponseDto {
        val dbResponse = repo.searchPortfolio()
        return SearchPortfolioResponseDto(
            foundPortfolios = dbResponse.result
                .map { portfolio -> portfolio.toResponsePortfolioDto() }
        )
    }
}