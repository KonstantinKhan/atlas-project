package com.khan366kos.atlas.project.backend.common.models.user

@JvmInline
value class UserName(val value: String) {
    companion object {
        val NONE = UserName("")
    }
}
