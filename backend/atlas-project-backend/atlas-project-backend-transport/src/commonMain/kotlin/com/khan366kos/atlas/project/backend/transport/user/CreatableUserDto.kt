package com.khan366kos.atlas.project.backend.transport.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreatableUserDto(
    @SerialName("name")
    val name: String,
    @SerialName("age")
    val age: Int,
    @SerialName("role")
    val role: String,
)
