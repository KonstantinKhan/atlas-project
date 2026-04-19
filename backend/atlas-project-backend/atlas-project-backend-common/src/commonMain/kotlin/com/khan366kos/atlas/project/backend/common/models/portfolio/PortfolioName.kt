package com.khan366kos.atlas.project.backend.common.models.portfolio

@JvmInline
value class PortfolioName(val value: String) {
    companion object {
        val NONE = PortfolioName("")
    }
}
