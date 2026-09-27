package dev.kbwallet.server.data

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

data class DatabaseConfig(
    val jdbcUrl: String,
    val user: String,
    val password: String,
)

/**
 * Connects Exposed to Postgres via a pooled HikariCP DataSource and ensures
 * the schema exists.
 *
 * `SchemaUtils.create` is a create-if-missing DDL call, not a real migration
 * tool (no versioning, no ALTER TABLE for schema changes) — fine while the
 * schema is this small and single-table, but reach for Flyway/Liquibase
 * before this needs its first real migration.
 */
object DatabaseFactory {
    fun connect(config: DatabaseConfig): Database {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = config.jdbcUrl
            username = config.user
            password = config.password
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        val dataSource = HikariDataSource(hikariConfig)
        val database = Database.connect(dataSource)
        transaction(database) {
            SchemaUtils.create(UsersTable)
        }
        return database
    }
}
