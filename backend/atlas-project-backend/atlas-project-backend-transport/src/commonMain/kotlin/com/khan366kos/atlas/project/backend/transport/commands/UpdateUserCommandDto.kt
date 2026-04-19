package com.khan366kos.atlas.project.backend.transport.commands

import com.khan366kos.atlas.project.backend.transport.user.UpdatableUserDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserCommandDto(
    @SerialName("updateUser")
    val updateUser: UpdatableUserDto,
)
