package com.khan366kos.atlas.project.backend.common.repo.user

import com.khan366kos.atlas.project.backend.common.models.user.User

sealed class UserRepoResult {
    data class Single(val user: User) : UserRepoResult()
    data class Multiple(val users: List<User>) : UserRepoResult()
    data object NotFound : UserRepoResult()
    data class DbError(val cause: Throwable) : UserRepoResult()
}
