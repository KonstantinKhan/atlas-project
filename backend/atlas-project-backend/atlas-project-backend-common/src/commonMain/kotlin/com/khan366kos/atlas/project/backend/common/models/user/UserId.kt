package com.khan366kos.atlas.project.backend.common.models.user

import java.util.UUID

@JvmInline
value class UserId(val value: String) {
    constructor(id: UUID) : this(id.toString())
    fun asString() = value
    fun asUUID(): UUID = UUID.fromString(value)
    companion object {
        val NONE = UserId("")
    }
}
