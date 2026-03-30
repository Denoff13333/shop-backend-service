package com.example.shop.app.integration

import com.example.shop.app.config.DatabaseConfig
import com.example.shop.app.database.DatabaseFactory
import org.junit.jupiter.api.BeforeEach
import org.testcontainers.containers.PostgreSQLContainer

abstract class PostgresIntegrationTestSupport {
    protected lateinit var databaseFactory: DatabaseFactory

    companion object {
        @JvmStatic
        protected val postgres = PostgreSQLContainer("postgres:16-alpine").apply {
            withDatabaseName("shop_db")
            withUsername("shop")
            withPassword("shop")
            start()
        }
    }

    @BeforeEach
    fun setUpDatabase() {
        databaseFactory = DatabaseFactory(
            DatabaseConfig(
                jdbcUrl = postgres.jdbcUrl,
                username = postgres.username,
                password = postgres.password,
                driverClassName = "org.postgresql.Driver",
                maximumPoolSize = 3
            )
        )
        databaseFactory.init(migrate = true)
    }
}
