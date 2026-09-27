package dev.kbwallet.server.models

import kotlinx.serialization.Serializable

// ── Request DTOs ──

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    // Everything below is optional at signup — a real display name, avatar,
    // and bio can always be filled in later from the profile screen. Keeping
    // signup to just email+password lowers the barrier for a first account.
    val username: String? = null,
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

// ── Response DTOs ──

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserResponse
)

@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val username: String,
    val avatarUrl: String? = null,
    val bio: String? = null,
    val createdAt: Long
)

@Serializable
data class UpdateProfileRequest(
    val username: String? = null,
    val avatarUrl: String? = null,
    val bio: String? = null
)

@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String
)

// ── Extension functions ──

fun User.toResponse(): UserResponse = UserResponse(
    id = id,
    email = email,
    username = username,
    avatarUrl = avatarUrl,
    bio = bio,
    createdAt = createdAt
)
