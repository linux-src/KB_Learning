package dev.kbwallet.app.core.domain.user

import dev.kbwallet.app.core.domain.Error
import kotlinx.serialization.Serializable

// ── Auth request DTOs ──

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    // Optional at signup — the server generates a default handle when this
    // is omitted, so account creation only ever requires an email+password.
    val username: String? = null,
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

// ── Auth response DTOs ──

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: User
)

// ── API error ──

@Serializable
data class ApiError(
    val error: String,
    val message: String
)

/** Distinguishes the register/login-specific failure modes worth showing a different message for. */
enum class AuthError : Error {
    EMAIL_EXISTS,
    INVALID_CREDENTIALS,
    INVALID_INPUT,
    NETWORK,
}
