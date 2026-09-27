package dev.kbwallet.app.core.network.auth

import dev.kbwallet.app.core.domain.*
import dev.kbwallet.app.core.domain.user.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class AuthApiClient(
    private val httpClient: HttpClient,
    private val baseUrl: String = "http://10.0.2.2:8080" // Android emulator -> host
) {
    /**
     * [username] is optional — the server derives a default handle from the
     * email when omitted, so registration only ever strictly needs an
     * email+password. Anything else (username, avatar, bio) can be set later
     * from the profile screen.
     */
    suspend fun register(email: String, password: String, username: String? = null): Result<AuthResponse, AuthError> {
        return try {
            val response = httpClient.post("$baseUrl/api/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(email = email, password = password, username = username))
            }
            when (response.status) {
                HttpStatusCode.Created -> Result.Success(response.body<AuthResponse>())
                HttpStatusCode.Conflict -> Result.Error(AuthError.EMAIL_EXISTS)
                HttpStatusCode.BadRequest -> Result.Error(AuthError.INVALID_INPUT)
                else -> Result.Error(AuthError.NETWORK)
            }
        } catch (e: Exception) {
            Result.Error(AuthError.NETWORK)
        }
    }

    suspend fun login(email: String, password: String): Result<AuthResponse, AuthError> {
        return try {
            val response = httpClient.post("$baseUrl/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email, password))
            }
            when (response.status) {
                HttpStatusCode.OK -> Result.Success(response.body<AuthResponse>())
                HttpStatusCode.Unauthorized -> Result.Error(AuthError.INVALID_CREDENTIALS)
                HttpStatusCode.BadRequest -> Result.Error(AuthError.INVALID_INPUT)
                else -> Result.Error(AuthError.NETWORK)
            }
        } catch (e: Exception) {
            Result.Error(AuthError.NETWORK)
        }
    }

    suspend fun refreshToken(refreshToken: String): Result<AuthResponse, DataError> {
        return try {
            val response = httpClient.post("$baseUrl/api/auth/refresh") {
                headers { append("Authorization", "Bearer $refreshToken") }
            }
            if (response.status == HttpStatusCode.OK) {
                Result.Success(response.body<AuthResponse>())
            } else {
                Result.Error(DataError.Remote.SERVER)
            }
        } catch (e: Exception) {
            Result.Error(mapNetworkException(e))
        }
    }

    suspend fun getProfile(accessToken: String): Result<User, DataError> {
        return try {
            val response = httpClient.get("$baseUrl/api/user/profile") {
                headers { append("Authorization", "Bearer $accessToken") }
            }
            if (response.status == HttpStatusCode.OK) {
                Result.Success(response.body<User>())
            } else {
                Result.Error(DataError.Remote.SERVER)
            }
        } catch (e: Exception) {
            Result.Error(mapNetworkException(e))
        }
    }

    private fun mapNetworkException(e: Exception): DataError.Remote {
        val message = e.message ?: ""
        return when {
            message.contains("timeout", ignoreCase = true) -> DataError.Remote.REQUEST_TIMEOUT
            message.contains("too many", ignoreCase = true) -> DataError.Remote.TOO_MANY_REQUESTS
            message.contains("unreachable", ignoreCase = true) || message.contains("connect", ignoreCase = true) -> DataError.Remote.NO_INTERNET
            else -> DataError.Remote.UNKNOWN
        }
    }
}
