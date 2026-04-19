package com.khan366kos.atlas.project.backend.mappers

import com.khan366kos.atlas.project.backend.common.models.user.User
import com.khan366kos.atlas.project.backend.common.models.user.UserAge
import com.khan366kos.atlas.project.backend.common.models.user.UserId
import com.khan366kos.atlas.project.backend.common.models.user.UserName
import com.khan366kos.atlas.project.backend.common.models.user.UserRole
import com.khan366kos.atlas.project.backend.transport.user.CreatableUserDto
import com.khan366kos.atlas.project.backend.transport.user.UpdatableUserDto
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class UserMappersTest : ShouldSpec({

    context("User.toDto()") {
        should("map all fields correctly") {
            val user = User(
                id = UserId("550e8400-e29b-41d4-a716-446655440000"),
                name = UserName("Alice"),
                age = UserAge(30),
                role = UserRole.ADMIN,
            )
            val dto = user.toDto()
            dto.id   shouldBe "550e8400-e29b-41d4-a716-446655440000"
            dto.name shouldBe "Alice"
            dto.age  shouldBe 30
            dto.role shouldBe "ADMIN"
        }

        should("map NONE user to empty id and default role") {
            val dto = User.NONE.toDto()
            dto.id   shouldBe ""
            dto.name shouldBe ""
            dto.age  shouldBe 0
            dto.role shouldBe "MEMBER"
        }
    }

    context("CreatableUserDto.toDomain()") {
        should("map all fields and set id to NONE") {
            val dto = CreatableUserDto(name = "Bob", age = 25, role = "MANAGER")
            val user = dto.toDomain()
            user.id   shouldBe UserId.NONE
            user.name shouldBe UserName("Bob")
            user.age  shouldBe UserAge(25)
            user.role shouldBe UserRole.MANAGER
        }

        should("throw IllegalArgumentException on unknown role") {
            val dto = CreatableUserDto(name = "X", age = 20, role = "UNKNOWN_ROLE")
            shouldThrow<IllegalArgumentException> { dto.toDomain() }
        }
    }

    context("User.applyUpdate(UpdatableUserDto)") {
        val base = User(
            id = UserId("id-1"),
            name = UserName("Alice"),
            age = UserAge(30),
            role = UserRole.MEMBER,
        )

        should("update all fields when all provided") {
            val dto = UpdatableUserDto(id = "id-1", name = "Bob", age = 40, role = "ADMIN")
            val updated = base.applyUpdate(dto)
            updated.name shouldBe UserName("Bob")
            updated.age  shouldBe UserAge(40)
            updated.role shouldBe UserRole.ADMIN
        }

        should("keep original fields when dto fields are null") {
            val dto = UpdatableUserDto(id = "id-1")
            val updated = base.applyUpdate(dto)
            updated.name shouldBe base.name
            updated.age  shouldBe base.age
            updated.role shouldBe base.role
        }

        should("update only name when only name provided") {
            val dto = UpdatableUserDto(id = "id-1", name = "Carol")
            val updated = base.applyUpdate(dto)
            updated.name shouldBe UserName("Carol")
            updated.age  shouldBe base.age
            updated.role shouldBe base.role
        }
    }
})
