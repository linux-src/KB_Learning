package dev.kbwallet.server.routes

import dev.kbwallet.server.models.*
import dev.kbwallet.server.plugins.getTokenService
import dev.kbwallet.server.plugins.getUserRepository
import dev.kbwallet.server.plugins.getPasswordHasher
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRouting() {
    route("/api/auth") {

        post("/register") {
            val request = call.receive<RegisterRequest>()

            // Validation — email + password are the only hard requirements.
            // Username, if provided, still has to look like a real handle;
            // if omitted, one is generated below instead of rejecting the request.
            if (request.email.isBlank() || !request.email.contains("@")) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("invalid_email", "A valid email is required"))
                return@post
            }
            if (request.username != null && request.username.length < 3) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("invalid_username", "Username must be at least 3 characters"))
                return@post
            }
            if (request.password.length < 6) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("weak_password", "Password must be at least 6 characters"))
                return@post
            }

            val userRepository = call.getUserRepository()
            val hasher = call.getPasswordHasher()

            // Check if user already exists
            val existing = userRepository.findByEmail(request.email)
            if (existing != null) {
                call.respond(HttpStatusCode.Conflict, ErrorResponse("email_exists", "User with this email already exists"))
                return@post
            }

            val username = request.username ?: generateUsername(request.email)

            // Create user
            val passwordHash = hasher.hash(request.password)
            val user = User(
                email = request.email,
                username = username,
                passwordHash = passwordHash
            )
            val createdUser = userRepository.create(user)

            // Generate tokens
            val tokenService = call.getTokenService()
            val accessToken = tokenService.generateAccessToken(createdUser.id, createdUser.email)
            val refreshToken = tokenService.generateRefreshToken(createdUser.id)

            call.respond(
                HttpStatusCode.Created,
                AuthResponse(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    user = createdUser.toResponse()
                )
            )
        }

        post("/login") {
            val request = call.receive<LoginRequest>()

            if (request.email.isBlank() || request.password.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("invalid_credentials", "Email and password are required"))
                return@post
            }

            val userRepository = call.getUserRepository()
            val hasher = call.getPasswordHasher()

            val user = userRepository.findByEmail(request.email)
            if (user == null) {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("invalid_credentials", "Invalid email or password"))
                return@post
            }

            if (!hasher.verify(request.password, user.passwordHash)) {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("invalid_credentials", "Invalid email or password"))
                return@post
            }

            val tokenService = call.getTokenService()
            val accessToken = tokenService.generateAccessToken(user.id, user.email)
            val refreshToken = tokenService.generateRefreshToken(user.id)

            call.respond(
                HttpStatusCode.OK,
                AuthResponse(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    user = user.toResponse()
                )
            )
        }

        post("/refresh") {
            val refreshToken = call.request.headers["Authorization"]?.removePrefix("Bearer ")
                ?: run {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("missing_token", "Refresh token is required"))
                    return@post
                }

            val tokenService = call.getTokenService()
            val userId = tokenService.validateRefreshToken(refreshToken)
            if (userId == null) {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("invalid_token", "Invalid or expired refresh token"))
                return@post
            }

            val userRepository = call.getUserRepository()
            val user = userRepository.findById(userId)
            if (user == null) {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("user_not_found", "User not found"))
                return@post
            }

            val newAccessToken = tokenService.generateAccessToken(user.id, user.email)
            val newRefreshToken = tokenService.generateRefreshToken(user.id)

            call.respond(
                HttpStatusCode.OK,
                AuthResponse(
                    accessToken = newAccessToken,
                    refreshToken = newRefreshToken,
                    user = user.toResponse()
                )
            )
        }
    }
}

/**
 * Derives a default handle from the email's local part when signup omits a
 * username, e.g. "alex@example.com" -> "alex7421". Usernames aren't
 * uniqueness-enforced at the DB level (unlike email) — this random 4-digit
 * suffix makes a collision negligible without needing an extra repository
 * lookup, appropriate for a cosmetic display name the user can change later.
 */
private fun generateUsername(email: String): String {
    val localPart = email.substringBefore("@")
        .filter { it.isLetterOrDigit() }
        .take(16)
        .ifEmpty { "user" }
    val suffix = (1000..9999).random()
    return "$localPart$suffix"
}
