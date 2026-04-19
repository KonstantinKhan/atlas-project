package com.khan366kos.atlas.project.backend.common.models.user

@JvmInline
value class UserAge(val value: Int) {
    companion object {
        val NONE = UserAge(0)
    }
}
