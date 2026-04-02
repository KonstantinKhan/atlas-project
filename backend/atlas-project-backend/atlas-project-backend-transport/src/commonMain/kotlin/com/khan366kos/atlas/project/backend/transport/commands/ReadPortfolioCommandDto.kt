package com.khan366kos.atlas.project.backend.transport.commands

import kotlinx.serialization.Serializable

@Serializable
data class ReadPortfolioCommandDto(
    val readPortfolioId: String? = null,
)
