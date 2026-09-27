package dev.kbwallet.server.plugins

import dev.kbwallet.server.data.DatabaseConfig
import dev.kbwallet.server.data.DatabaseFactory
import dev.kbwallet.server.data.ExposedUserRepository
import dev.kbwallet.server.data.InMemoryUserRepository
import dev.kbwallet.server.data.UserRepository
import dev.kbwallet.server.security.PasswordHasher
import dev.kbwallet.server.security.TokenConfig
import dev.kbwallet.server.security.TokenService
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.util.AttributeKey

fun Application.configureSecurity() {
    val jwtSecret = environment.config.property("jwt.secret").getString()
    val jwtIssuer = environment.config.property("jwt.issuer").getString()
    val jwtAudience = environment.config.property("jwt.audience").getString()
    val accessTokenExpirationMs = environment.config.property("jwt.accessTokenExpirationMs").getString().toLong()
    val refreshTokenExpirationMs = environment.config.property("jwt.refreshTokenExpirationMs").getString().toLong()
    val allowedCorsHosts = environment.config.propertyOrNull("cors.allowedHosts")
        ?.getString()
        ?.split(",")
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        .orEmpty()

    val tokenConfig = TokenConfig(
        secret = jwtSecret,
        issuer = jwtIssuer,
        audience = jwtAudience,
        accessTokenExpirationMs = accessTokenExpirationMs,
        refreshTokenExpirationMs = refreshTokenExpirationMs
    )

    val tokenService = TokenService(tokenConfig)

    // Store in application attributes for DI
    attributes.put(TokenServiceKey, tokenService)

    install(Authentication) {
        jwt("auth-jwt") {
            realm = "KB Wallet API"
            verifier(
                JWT.require(Algorithm.HMAC256(tokenConfig.secret))
                    .withIssuer(tokenConfig.issuer)
                    .withAudience(tokenConfig.audience)
                    .withSubject("access")
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("userId").asString() != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }

    install(CORS) {
        // Auth is bearer-token (Authorization header), not cookies, so credentials
        // mode isn't needed here — and anyHost() + credentials is the exact
        // combination browsers/Ktor warn about. Explicit host allowlist instead.
        allowedCorsHosts.forEach { host -> allowHost(host, schemes = listOf("http", "https")) }
        allowHeader("Authorization")
        allowHeader("Content-Type")
        allowMethod(io.ktor.http.HttpMethod.Post)
        allowMethod(io.ktor.http.HttpMethod.Get)
        allowMethod(io.ktor.http.HttpMethod.Put)
        allowMethod(io.ktor.http.HttpMethod.Delete)
    }
}

fun Application.configureDependencies() {
    val iterations = environment.config.property("password.iterations").getString().toInt()
    val keyLength = environment.config.property("password.keyLength").getString().toInt()

    val databaseEnabled = environment.config.propertyOrNull("database.enabled")?.getString().toBoolean()
    val userRepository: UserRepository = if (databaseEnabled) {
        val dbConfig = DatabaseConfig(
            jdbcUrl = environment.config.property("database.url").getString(),
            user = environment.config.property("database.user").getString(),
            password = environment.config.property("database.password").getString(),
        )
        DatabaseFactory.connect(dbConfig)
        ExposedUserRepository()
    } else {
        log.warn(
            "database.enabled is false — using InMemoryUserRepository, all user " +
                "data will be LOST on restart. Set DATABASE_ENABLED=true (and " +
                "DATABASE_URL/_USER/_PASSWORD) for real/persistent usage."
        )
        InMemoryUserRepository()
    }

    attributes.put(UserRepositoryKey, userRepository)
    attributes.put(PasswordHasherKey, PasswordHasher(iterations, keyLength))
}

val TokenServiceKey = AttributeKey<TokenService>("TokenService")
val UserRepositoryKey = AttributeKey<UserRepository>("UserRepository")
val PasswordHasherKey = AttributeKey<PasswordHasher>("PasswordHasher")

fun ApplicationCall.getTokenService(): TokenService =
    application.attributes[TokenServiceKey]

fun ApplicationCall.getUserRepository(): UserRepository =
    application.attributes[UserRepositoryKey]

fun ApplicationCall.getPasswordHasher(): PasswordHasher =
    application.attributes[PasswordHasherKey]
