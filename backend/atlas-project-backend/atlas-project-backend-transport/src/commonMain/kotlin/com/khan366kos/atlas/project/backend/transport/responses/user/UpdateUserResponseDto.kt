package com.khan366kos.atlas.project.backend.transport.responses.user

import com.khan366kos.atlas.project.backend.transport.user.UserResponseDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserResponseDto(
    @SerialName("messageType")
    val messageType: String,
    @SerialName("updatedUser")
    val updatedUser: UserResponseDto,
)
