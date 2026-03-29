package com.khan366kos.atlas.project.backend.common.project

import java.util.UUID

@JvmInline
value class PortfolioProjectId(private val value: String) {

    constructor(id: UUID) : this(id.toString())

    fun asString() = value

    companion object {
        val NONE = PortfolioProjectId("")
    }
}
