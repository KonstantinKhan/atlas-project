package com.khan366kos.atlas.project.backend.common.repo.portfolio

import com.khan366kos.atlas.project.backend.common.models.portfolio.Portfolio

data class DbPortfolioResponse(
    val success: Boolean,
    val result: Portfolio
)