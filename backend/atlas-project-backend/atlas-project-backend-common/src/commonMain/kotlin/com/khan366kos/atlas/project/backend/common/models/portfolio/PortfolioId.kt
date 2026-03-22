package com.khan366kos.atlas.project.backend.common.models.portfolio

@JvmInline
value class PortfolioId(val value: String) {
    fun asString() = value

    companion object {
        val NONE = PortfolioId("")
    }
}
