package com.khan366kos

import com.khan366kos.atlas.project.backend.common.models.user.UserId
import com.khan366kos.atlas.project.backend.common.repo.user.DbUserFilterRequest
import com.khan366kos.atlas.project.backend.common.repo.user.DbUserIdRequest
import com.khan366kos.atlas.project.backend.common.repo.user.DbUserRequest
import com.khan366kos.atlas.project.backend.common.repo.user.IUserRepo
import com.khan366kos.atlas.project.backend.common.repo.user.UserRepoResult
import com.khan366kos.atlas.project.backend.mappers.applyUpdate
import com.khan366kos.atlas.project.backend.mappers.toDto
import com.khan366kos.atlas.project.backend.mappers.toDomain
import com.khan366kos.atlas.project.backend.transport.commands.CreateUserCommandDto
import com.khan366kos.atlas.project.backend.transport.commands.DeleteUserCommandDto
import com.khan366kos.atlas.project.backend.transport.commands.UpdateUserCommandDto
import com.khan366kos.atlas.project.backend.transport.queries.GetUserQuery
import com.khan366kos.atlas.project.backend.transport.queries.ListUsersQuery
import com.khan366kos.atlas.project.backend.transport.responses.user.CreateUserResponseDto
import com.khan366kos.atlas.project.backend.transport.responses.user.DeleteUserResponseDto
import com.khan366kos.atlas.project.backend.transport.responses.user.ListUsersResponseDto
import com.khan366kos.atlas.project.backend.transport.responses.user.ReadUserResponseDto
import com.khan366kos.atlas.project.backend.transport.responses.user.UpdateUserResponseDto

class UserService(private val repo: IUserRepo) {

    suspend fun create(cmd: CreateUserCommandDto): CreateUserResponseDto {
        val user = cmd.createUser.toDomain()
        return when (val result = repo.createUser(DbUserRequest(user))) {
            is UserRepoResult.Single -> CreateUserResponseDto(
                messageType = "createUser",
                createdUser = result.user.toDto(),
            )
            is UserRepoResult.DbError -> throw RuntimeException("Failed to create user", result.cause)
            else -> throw RuntimeException("Unexpected result on create user")
        }
    }

    suspend fun read(query: GetUserQuery): ReadUserResponseDto {
        return when (val result = repo.readUser(DbUserIdRequest(query.getUserId))) {
            is UserRepoResult.Single -> ReadUserResponseDto(
                messageType = "readUser",
                readUser = result.user.toDto(),
            )
            is UserRepoResult.NotFound -> throw NoSuchElementException("User not found: ${query.getUserId}")
            is UserRepoResult.DbError -> throw RuntimeException("Failed to read user", result.cause)
            else -> throw RuntimeException("Unexpected result on read user")
        }
    }

    suspend fun update(cmd: UpdateUserCommandDto): UpdateUserResponseDto {
        val userId = cmd.updateUser.id
        val existing = when (val result = repo.readUser(DbUserIdRequest(userId))) {
            is UserRepoResult.Single -> result.user
            is UserRepoResult.NotFound -> throw NoSuchElementException("User not found: $userId")
            is UserRepoResult.DbError -> throw RuntimeException("Failed to read user", result.cause)
            else -> throw RuntimeException("Unexpected result on read user")
        }
        val updated = existing.applyUpdate(cmd.updateUser)
        return when (val result = repo.updateUser(DbUserRequest(updated))) {
            is UserRepoResult.Single -> UpdateUserResponseDto(
                messageType = "updateUser",
                updatedUser = result.user.toDto(),
            )
            is UserRepoResult.NotFound -> throw NoSuchElementException("User not found: $userId")
            is UserRepoResult.DbError -> throw RuntimeException("Failed to update user", result.cause)
            else -> throw RuntimeException("Unexpected result on update user")
        }
    }

    suspend fun delete(cmd: DeleteUserCommandDto): DeleteUserResponseDto {
        return when (val result = repo.deleteUser(DbUserIdRequest(cmd.deleteUserId))) {
            is UserRepoResult.Single -> DeleteUserResponseDto(
                messageType = "deleteUser",
                deletedUser = result.user.toDto(),
            )
            is UserRepoResult.NotFound -> throw NoSuchElementException("User not found: ${cmd.deleteUserId}")
            is UserRepoResult.DbError -> throw RuntimeException("Failed to delete user", result.cause)
            else -> throw RuntimeException("Unexpected result on delete user")
        }
    }

    suspend fun search(query: ListUsersQuery): ListUsersResponseDto {
        val roleEnum = query.role?.let {
            runCatching {
                com.khan366kos.atlas.project.backend.common.models.user.UserRole.valueOf(it)
            }.getOrNull()
        }
        val filter = DbUserFilterRequest(name = query.name, role = roleEnum)
        return when (val result = repo.searchUsers(filter)) {
            is UserRepoResult.Multiple -> ListUsersResponseDto(
                messageType = "listUsers",
                users = result.users.map { it.toDto() },
            )
            is UserRepoResult.DbError -> throw RuntimeException("Failed to search users", result.cause)
            else -> ListUsersResponseDto(messageType = "listUsers", users = emptyList())
        }
    }
}
