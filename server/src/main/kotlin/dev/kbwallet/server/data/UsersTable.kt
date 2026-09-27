package dev.kbwallet.server.data

import org.jetbrains.exposed.sql.Table

/** Exposed schema for [dev.kbwallet.server.models.User]. */
object UsersTable : Table("users") {
    val id = varchar("id", 36)
    val email = varchar("email", 255).uniqueIndex()
    val username = varchar("username", 64)
    val passwordHash = varchar("password_hash", 255)
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")
    val avatarUrl = varchar("avatar_url", 512).nullable()
    val bio = text("bio").nullable()

    override val primaryKey = PrimaryKey(id)
}
