package com.khan366kos.atlas.project.backend.common.repo.portfolio

import com.khan366kos.atlas.project.backend.common.models.portfolio.Portfolio

class DbPortfoliosResponse(
    val isSuccess: Boolean,
    val result: List<Portfolio>
) {
}