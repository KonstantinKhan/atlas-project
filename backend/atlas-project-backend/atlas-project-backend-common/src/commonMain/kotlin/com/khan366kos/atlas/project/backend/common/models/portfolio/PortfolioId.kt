package com.khan366kos.atlas.project.backend.common.models.portfolio

import java.util.UUID

@JvmInline
value class PortfolioId(private val value: String) {

    constructor(id: UUID) : this(id.toString())

    fun asString() = value

    fun asUUID(): UUID = UUID.fromString(value)

    companion object {
        val NONE = PortfolioId("")
    }
}
