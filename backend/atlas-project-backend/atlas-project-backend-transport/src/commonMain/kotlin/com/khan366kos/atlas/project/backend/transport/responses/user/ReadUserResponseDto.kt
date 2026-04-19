package com.khan366kos.atlas.project.backend.transport.responses.user

import com.khan366kos.atlas.project.backend.transport.user.UserResponseDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReadUserResponseDto(
    @SerialName("messageType")
    val messageType: String,
    @SerialName("readUser")
    val readUser: UserResponseDto,
)
