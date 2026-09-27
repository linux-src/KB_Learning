package dev.kbwallet.server.data

import dev.kbwallet.server.models.User

interface UserRepository {
    suspend fun findByEmail(email: String): User?
    suspend fun findById(id: String): User?
    suspend fun create(user: User): User
    suspend fun update(user: User): User
    suspend fun delete(id: String): Boolean
    suspend fun count(): Int
}
