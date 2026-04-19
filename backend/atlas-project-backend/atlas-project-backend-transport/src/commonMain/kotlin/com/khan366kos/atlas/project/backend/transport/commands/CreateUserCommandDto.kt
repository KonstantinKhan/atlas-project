package com.khan366kos.atlas.project.backend.transport.commands

import com.khan366kos.atlas.project.backend.transport.user.CreatableUserDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateUserCommandDto(
    @SerialName("createUser")
    val createUser: CreatableUserDto,
)
