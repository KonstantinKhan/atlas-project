package com.khan366kos.atlas.project.backend.common.models.user

data class User(
    val id: UserId = UserId.NONE,
    val name: UserName = UserName.NONE,
    val age: UserAge = UserAge.NONE,
    val role: UserRole = UserRole.MEMBER,
) {
    companion object {
        val NONE = User()
    }
}
