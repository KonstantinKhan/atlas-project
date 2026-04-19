package com.khan366kos.atlas.project.backend.transport.commands

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeleteUserCommandDto(
    @SerialName("deleteUserId")
    val deleteUserId: String,
)
