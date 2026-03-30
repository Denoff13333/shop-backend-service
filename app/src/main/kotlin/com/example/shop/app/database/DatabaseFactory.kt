package com.example.shop.app.database

import com.example.shop.app.config.DatabaseConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import javax.sql.DataSource

class DatabaseFactory(private val config: DatabaseConfig) {
    lateinit var database: Database
        private set

    fun init(migrate: Boolean = true) {
        val hikariDataSource = createDataSource()
        database = Database.connect(hikariDataSource)
        if (migrate) {
            Flyway.configure()
                .dataSource(hikariDataSource)
                .locations("classpath:db/migration")
                .load()
                .migrate()
        }
    }

    private fun createDataSource(): DataSource {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = config.jdbcUrl
            username = config.username
            password = config.password
            driverClassName = config.driverClassName
            maximumPoolSize = config.maximumPoolSize
        }
        return HikariDataSource(hikariConfig)
    }

    suspend fun <T> dbQuery(block: () -> T): T =
        withContext(Dispatchers.IO) {
            transaction(database) {
                block()
            }
        }
}
