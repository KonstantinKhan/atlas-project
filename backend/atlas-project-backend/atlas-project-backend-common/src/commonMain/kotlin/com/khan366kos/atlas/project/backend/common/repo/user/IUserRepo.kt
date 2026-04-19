package com.khan366kos.atlas.project.backend.common.repo.user

interface IUserRepo {
    suspend fun createUser(request: DbUserRequest): UserRepoResult
    suspend fun readUser(request: DbUserIdRequest): UserRepoResult
    suspend fun updateUser(request: DbUserRequest): UserRepoResult
    suspend fun deleteUser(request: DbUserIdRequest): UserRepoResult
    suspend fun searchUsers(request: DbUserFilterRequest): UserRepoResult
}
