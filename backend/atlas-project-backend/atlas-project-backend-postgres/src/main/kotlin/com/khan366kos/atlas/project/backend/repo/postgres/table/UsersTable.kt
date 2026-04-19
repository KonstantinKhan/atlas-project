package com.khan366kos.atlas.project.backend.repo.postgres.table

import org.jetbrains.exposed.sql.Table

object UsersTable : Table("users") {
    val id = uuid("id")
    val name = varchar("name", 255)
    val age = integer("age")
    val role = varchar("role", 50)

    override val primaryKey = PrimaryKey(id)
}
