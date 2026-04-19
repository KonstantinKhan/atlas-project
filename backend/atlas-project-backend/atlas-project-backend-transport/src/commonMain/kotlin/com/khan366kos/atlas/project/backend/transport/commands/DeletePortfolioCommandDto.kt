package com.khan366kos.atlas.project.backend.transport.commands

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class DeletePortfolioCommandDto(
    @SerialName("deletePortfolioId")
    val deletePortfolioId: String,
)
