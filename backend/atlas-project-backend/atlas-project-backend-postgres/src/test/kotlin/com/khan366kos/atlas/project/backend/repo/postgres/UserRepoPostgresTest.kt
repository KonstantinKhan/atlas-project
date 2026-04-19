package com.khan366kos.atlas.project.backend.repo.postgres

import com.khan366kos.atlas.project.backend.common.models.user.User
import com.khan366kos.atlas.project.backend.common.models.user.UserAge
import com.khan366kos.atlas.project.backend.common.models.user.UserId
import com.khan366kos.atlas.project.backend.common.models.user.UserName
import com.khan366kos.atlas.project.backend.common.models.user.UserRole
import com.khan366kos.atlas.project.backend.common.repo.user.DbUserFilterRequest
import com.khan366kos.atlas.project.backend.common.repo.user.DbUserIdRequest
import com.khan366kos.atlas.project.backend.common.repo.user.DbUserRequest
import com.khan366kos.atlas.project.backend.common.repo.user.UserRepoResult
import com.khan366kos.atlas.project.backend.repo.postgres.table.UsersTable
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class UserRepoPostgresTest : ShouldSpec({
    val db = Database.connect(
        url = "jdbc:h2:mem:test_users_${UUID.randomUUID()};DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        driver = "org.h2.Driver",
    )
    val repo = UserRepoPostgres(db)

    beforeSpec {
        transaction(db) { SchemaUtils.create(UsersTable) }
    }

    afterSpec {
        transaction(db) { SchemaUtils.drop(UsersTable) }
    }

    beforeTest {
        transaction(db) { UsersTable.deleteAll() }
    }

    fun newUser(name: String = "Alice", age: Int = 30, role: UserRole = UserRole.MEMBER) = User(
        id = UserId.NONE,
        name = UserName(name),
        age = UserAge(age),
        role = role,
    )

    context("createUser") {
        should("return Single with generated id") {
            val result = repo.createUser(DbUserRequest(newUser()))
            val single = result.shouldBeInstanceOf<UserRepoResult.Single>()
            single.user.id.value shouldNotBe ""
        }

        should("persist user so it can be read back") {
            val created = (repo.createUser(DbUserRequest(newUser("Bob", 25, UserRole.ADMIN))) as UserRepoResult.Single).user
            val found = repo.readUser(DbUserIdRequest(created.id.value))
            (found as UserRepoResult.Single).user.name shouldBe UserName("Bob")
        }
    }

    context("readUser") {
        should("return Single when user exists") {
            val created = (repo.createUser(DbUserRequest(newUser())) as UserRepoResult.Single).user
            val result = repo.readUser(DbUserIdRequest(created.id.value))
            result.shouldBeInstanceOf<UserRepoResult.Single>()
        }

        should("return NotFound for unknown id") {
            val result = repo.readUser(DbUserIdRequest(UUID.randomUUID().toString()))
            result shouldBe UserRepoResult.NotFound
        }
    }

    context("updateUser") {
        should("update fields and return Single") {
            val created = (repo.createUser(DbUserRequest(newUser())) as UserRepoResult.Single).user
            val updated = created.copy(name = UserName("Updated"), age = UserAge(99), role = UserRole.ADMIN)
            val result = repo.updateUser(DbUserRequest(updated))
            val single = result.shouldBeInstanceOf<UserRepoResult.Single>()
            single.user.name shouldBe UserName("Updated")
        }

        should("return NotFound when user does not exist") {
            val ghost = newUser().copy(id = UserId(UUID.randomUUID().toString()))
            val result = repo.updateUser(DbUserRequest(ghost))
            result shouldBe UserRepoResult.NotFound
        }
    }

    context("deleteUser") {
        should("delete user and return its data") {
            val created = (repo.createUser(DbUserRequest(newUser())) as UserRepoResult.Single).user
            val result = repo.deleteUser(DbUserIdRequest(created.id.value))
            result.shouldBeInstanceOf<UserRepoResult.Single>()
            repo.readUser(DbUserIdRequest(created.id.value)) shouldBe UserRepoResult.NotFound
        }

        should("return NotFound for unknown id") {
            val result = repo.deleteUser(DbUserIdRequest(UUID.randomUUID().toString()))
            result shouldBe UserRepoResult.NotFound
        }
    }

    context("searchUsers") {
        should("return all users when no filters") {
            repo.createUser(DbUserRequest(newUser("Alice")))
            repo.createUser(DbUserRequest(newUser("Bob")))
            val result = repo.searchUsers(DbUserFilterRequest())
            (result as UserRepoResult.Multiple).users shouldHaveSize 2
        }

        should("filter by name substring") {
            repo.createUser(DbUserRequest(newUser("Alice")))
            repo.createUser(DbUserRequest(newUser("Bob")))
            val result = repo.searchUsers(DbUserFilterRequest(name = "Ali"))
            (result as UserRepoResult.Multiple).users shouldHaveSize 1
            result.users[0].name shouldBe UserName("Alice")
        }

        should("filter by role") {
            repo.createUser(DbUserRequest(newUser(role = UserRole.ADMIN)))
            repo.createUser(DbUserRequest(newUser(role = UserRole.MEMBER)))
            val result = repo.searchUsers(DbUserFilterRequest(role = UserRole.ADMIN))
            (result as UserRepoResult.Multiple).users shouldHaveSize 1
        }

        should("return empty list when no match") {
            repo.createUser(DbUserRequest(newUser("Alice")))
            val result = repo.searchUsers(DbUserFilterRequest(name = "ZZZ"))
            (result as UserRepoResult.Multiple).users shouldHaveSize 0
        }
    }
})
