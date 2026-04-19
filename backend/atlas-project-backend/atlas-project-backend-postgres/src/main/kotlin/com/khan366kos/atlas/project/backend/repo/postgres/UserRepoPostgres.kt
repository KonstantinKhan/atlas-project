package com.khan366kos.atlas.project.backend.repo.postgres

import com.khan366kos.atlas.project.backend.common.models.user.User
import com.khan366kos.atlas.project.backend.common.models.user.UserAge
import com.khan366kos.atlas.project.backend.common.models.user.UserId
import com.khan366kos.atlas.project.backend.common.models.user.UserName
import com.khan366kos.atlas.project.backend.common.models.user.UserRole
import com.khan366kos.atlas.project.backend.common.repo.user.DbUserFilterRequest
import com.khan366kos.atlas.project.backend.common.repo.user.DbUserIdRequest
import com.khan366kos.atlas.project.backend.common.repo.user.DbUserRequest
import com.khan366kos.atlas.project.backend.common.repo.user.IUserRepo
import com.khan366kos.atlas.project.backend.common.repo.user.UserRepoResult
import com.khan366kos.atlas.project.backend.repo.postgres.table.UsersTable
import kotlinx.coroutines.CancellationException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

class UserRepoPostgres(private val database: Database) : IUserRepo {

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun createUser(request: DbUserRequest): UserRepoResult =
        newSuspendedTransaction(db = database) {
            runCatching {
                val newId = Uuid.random().toJavaUuid()
                UsersTable.insert {
                    it[id] = newId
                    it[name] = request.user.name.value
                    it[age] = request.user.age.value
                    it[role] = request.user.role.name
                }
                UserRepoResult.Single(request.user.copy(id = UserId(newId)))
            }
                .onFailure { if (it is CancellationException) throw it }
                .fold(
                    onSuccess = { it },
                    onFailure = { UserRepoResult.DbError(it) }
                )
        }

    override suspend fun readUser(request: DbUserIdRequest): UserRepoResult =
        newSuspendedTransaction(db = database) {
            runCatching {
                UsersTable.selectAll()
                    .where { UsersTable.id eq UUID.fromString(request.id) }
                    .singleOrNull()
                    ?.toUser()
            }
                .onFailure { if (it is CancellationException) throw it }
                .fold(
                    onSuccess = { user ->
                        user?.let { UserRepoResult.Single(it) } ?: UserRepoResult.NotFound
                    },
                    onFailure = { UserRepoResult.DbError(it) }
                )
        }

    override suspend fun updateUser(request: DbUserRequest): UserRepoResult =
        newSuspendedTransaction(db = database) {
            runCatching {
                val updated = UsersTable.update({ UsersTable.id eq request.user.id.asUUID() }) {
                    it[name] = request.user.name.value
                    it[age] = request.user.age.value
                    it[role] = request.user.role.name
                }
                if (updated == 0) null else request.user
            }
                .onFailure { if (it is CancellationException) throw it }
                .fold(
                    onSuccess = { user ->
                        user?.let { UserRepoResult.Single(it) } ?: UserRepoResult.NotFound
                    },
                    onFailure = { UserRepoResult.DbError(it) }
                )
        }

    override suspend fun deleteUser(request: DbUserIdRequest): UserRepoResult =
        newSuspendedTransaction(db = database) {
            runCatching {
                val row = UsersTable.selectAll()
                    .where { UsersTable.id eq UUID.fromString(request.id) }
                    .singleOrNull()
                    ?.toUser()
                if (row != null) {
                    UsersTable.deleteWhere { UsersTable.id eq UUID.fromString(request.id) }
                }
                row
            }
                .onFailure { if (it is CancellationException) throw it }
                .fold(
                    onSuccess = { user ->
                        user?.let { UserRepoResult.Single(it) } ?: UserRepoResult.NotFound
                    },
                    onFailure = { UserRepoResult.DbError(it) }
                )
        }

    override suspend fun searchUsers(request: DbUserFilterRequest): UserRepoResult =
        newSuspendedTransaction(db = database) {
            runCatching {
                var query = UsersTable.selectAll()
                request.name?.let { n -> query = query.andWhere { UsersTable.name like "%$n%" } }
                request.role?.let { r -> query = query.andWhere { UsersTable.role eq r.name } }
                query.map { it.toUser() }
            }
                .onFailure { if (it is CancellationException) throw it }
                .fold(
                    onSuccess = { UserRepoResult.Multiple(it) },
                    onFailure = { UserRepoResult.DbError(it) }
                )
        }

    private fun ResultRow.toUser() = User(
        id = UserId(this[UsersTable.id].toString()),
        name = UserName(this[UsersTable.name]),
        age = UserAge(this[UsersTable.age]),
        role = UserRole.valueOf(this[UsersTable.role]),
    )
}
