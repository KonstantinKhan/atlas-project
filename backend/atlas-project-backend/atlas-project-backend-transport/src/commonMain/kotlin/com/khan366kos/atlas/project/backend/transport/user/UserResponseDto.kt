package com.khan366kos.atlas.project.backend.transport.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserResponseDto(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("age")
    val age: Int,
    @SerialName("role")
    val role: String,
)
