package dev.kbwallet.server.data

import dev.kbwallet.server.models.User
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update

/** Real, durable [UserRepository] backed by Postgres via Exposed. */
class ExposedUserRepository : UserRepository {

    override suspend fun findByEmail(email: String): User? = newSuspendedTransaction(Dispatchers.IO) {
        UsersTable.selectAll()
            .where { UsersTable.email.lowerCase() eq email.lowercase() }
            .limit(1)
            .map { it.toUser() }
            .firstOrNull()
    }

    override suspend fun findById(id: String): User? = newSuspendedTransaction(Dispatchers.IO) {
        UsersTable.selectAll()
            .where { UsersTable.id eq id }
            .limit(1)
            .map { it.toUser() }
            .firstOrNull()
    }

    override suspend fun create(user: User): User = newSuspendedTransaction(Dispatchers.IO) {
        UsersTable.insert {
            it[id] = user.id
            it[email] = user.email
            it[username] = user.username
            it[passwordHash] = user.passwordHash
            it[createdAt] = user.createdAt
            it[updatedAt] = user.updatedAt
            it[avatarUrl] = user.avatarUrl
            it[bio] = user.bio
        }
        user
    }

    override suspend fun update(user: User): User = newSuspendedTransaction(Dispatchers.IO) {
        val updated = UsersTable.update({ UsersTable.id eq user.id }) {
            it[email] = user.email
            it[username] = user.username
            it[passwordHash] = user.passwordHash
            it[updatedAt] = user.updatedAt
            it[avatarUrl] = user.avatarUrl
            it[bio] = user.bio
        }
        if (updated == 0) throw NoSuchElementException("User not found: ${user.id}")
        user
    }

    override suspend fun delete(id: String): Boolean = newSuspendedTransaction(Dispatchers.IO) {
        // deleteWhere's op lambda is `T.(ISqlExpressionBuilder) -> Op<Boolean>`, not the
        // `SqlExpressionBuilder.() -> Op<Boolean>` shape where()/update() use — eq() is a
        // member of SqlExpressionBuilder, so it needs Op.build{} to get that receiver.
        UsersTable.deleteWhere { Op.build { UsersTable.id eq id } } > 0
    }

    override suspend fun count(): Int = newSuspendedTransaction(Dispatchers.IO) {
        UsersTable.selectAll().count().toInt()
    }

    private fun ResultRow.toUser() = User(
        id = this[UsersTable.id],
        email = this[UsersTable.email],
        username = this[UsersTable.username],
        passwordHash = this[UsersTable.passwordHash],
        createdAt = this[UsersTable.createdAt],
        updatedAt = this[UsersTable.updatedAt],
        avatarUrl = this[UsersTable.avatarUrl],
        bio = this[UsersTable.bio],
    )
}
