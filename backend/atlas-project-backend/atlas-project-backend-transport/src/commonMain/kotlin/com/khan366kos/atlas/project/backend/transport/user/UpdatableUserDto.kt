package com.khan366kos.atlas.project.backend.transport.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdatableUserDto(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String? = null,
    @SerialName("age")
    val age: Int? = null,
    @SerialName("role")
    val role: String? = null,
)
