package com.khan366kos.atlas.project.backend.common.models.portfolio

import java.util.UUID

@JvmInline
value class PortfolioId(val value: String) {

    constructor(id: UUID) : this(id.toString())

    fun asString() = value

    companion object {
        val NONE = PortfolioId("")
    }
}
