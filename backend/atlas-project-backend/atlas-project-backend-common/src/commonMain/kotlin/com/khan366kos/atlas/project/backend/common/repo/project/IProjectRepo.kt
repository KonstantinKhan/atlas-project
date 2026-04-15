package com.khan366kos.atlas.project.backend.common.repo.project

interface IProjectRepo {
    suspend fun search(request: DbProjectSearchRequest): PortfolioProjectRepoResult
}