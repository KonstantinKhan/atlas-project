package com.khan366kos.atlas.project.backend.transport.queries

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ListUsersQuery(
    @SerialName("name")
    val name: String? = null,
    @SerialName("role")
    val role: String? = null,
    @SerialName("page")
    val page: Int = 1,
    @SerialName("pageSize")
    val pageSize: Int = 20,
)
