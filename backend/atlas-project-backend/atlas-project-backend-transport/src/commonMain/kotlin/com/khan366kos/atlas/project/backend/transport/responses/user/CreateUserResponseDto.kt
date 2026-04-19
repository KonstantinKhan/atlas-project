package com.khan366kos.atlas.project.backend.transport.responses.user

import com.khan366kos.atlas.project.backend.transport.user.UserResponseDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateUserResponseDto(
    @SerialName("messageType")
    val messageType: String,
    @SerialName("createdUser")
    val createdUser: UserResponseDto,
)
