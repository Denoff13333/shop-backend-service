package com.example.shop.app.config

import io.ktor.server.application.ApplicationEnvironment

data class DatabaseConfig(
    val jdbcUrl: String,
    val username: String,
    val password: String,
    val driverClassName: String,
    val maximumPoolSize: Int
)

data class RedisConfig(
    val host: String,
    val port: Int,
    val ttlSeconds: Long
)

data class RabbitMqConfig(
    val host: String,
    val port: Int,
    val username: String,
    val password: String,
    val queue: String
)

data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String,
    val expiresInSeconds: Long
)

data class BootstrapConfig(
    val adminEmail: String,
    val adminPassword: String
)

data class AppConfig(
    val database: DatabaseConfig,
    val redis: RedisConfig,
    val rabbitMq: RabbitMqConfig,
    val jwt: JwtConfig,
    val bootstrap: BootstrapConfig
)

object AppConfigLoader {
    private fun readString(
        environment: ApplicationEnvironment,
        configKey: String,
        envKey: String,
        defaultValue: String
    ): String {
        return environment.config.propertyOrNull(configKey)?.getString()
            ?: System.getenv(envKey)
            ?: defaultValue
    }

    fun load(environment: ApplicationEnvironment): AppConfig {
        return AppConfig(
            database = DatabaseConfig(
                jdbcUrl = readString(
                    environment,
                    "app.database.jdbcUrl",
                    "DB_URL",
                    "jdbc:postgresql://localhost:5432/shop_db"
                ),
                username = readString(
                    environment,
                    "app.database.username",
                    "DB_USER",
                    "shop"
                ),
                password = readString(
                    environment,
                    "app.database.password",
                    "DB_PASSWORD",
                    "shop"
                ),
                driverClassName = readString(
                    environment,
                    "app.database.driverClassName",
                    "DB_DRIVER",
                    "org.postgresql.Driver"
                ),
                maximumPoolSize = readString(
                    environment,
                    "app.database.maximumPoolSize",
                    "DB_MAX_POOL_SIZE",
                    "10"
                ).toInt()
            ),
            redis = RedisConfig(
                host = readString(
                    environment,
                    "app.redis.host",
                    "REDIS_HOST",
                    "localhost"
                ),
                port = readString(
                    environment,
                    "app.redis.port",
                    "REDIS_PORT",
                    "6379"
                ).toInt(),
                ttlSeconds = readString(
                    environment,
                    "app.redis.ttlSeconds",
                    "CACHE_TTL_SECONDS",
                    "300"
                ).toLong()
            ),
            rabbitMq = RabbitMqConfig(
                host = readString(
                    environment,
                    "app.rabbitmq.host",
                    "RABBITMQ_HOST",
                    "localhost"
                ),
                port = readString(
                    environment,
                    "app.rabbitmq.port",
                    "RABBITMQ_PORT",
                    "5672"
                ).toInt(),
                username = readString(
                    environment,
                    "app.rabbitmq.username",
                    "RABBITMQ_USERNAME",
                    "guest"
                ),
                password = readString(
                    environment,
                    "app.rabbitmq.password",
                    "RABBITMQ_PASSWORD",
                    "guest"
                ),
                queue = readString(
                    environment,
                    "app.rabbitmq.queue",
                    "RABBITMQ_QUEUE",
                    "order-events"
                )
            ),
            jwt = JwtConfig(
                secret = readString(
                    environment,
                    "app.jwt.secret",
                    "JWT_SECRET",
                    "super-secret-change-me"
                ),
                issuer = readString(
                    environment,
                    "app.jwt.issuer",
                    "JWT_ISSUER",
                    "shop-service"
                ),
                audience = readString(
                    environment,
                    "app.jwt.audience",
                    "JWT_AUDIENCE",
                    "shop-users"
                ),
                realm = readString(
                    environment,
                    "app.jwt.realm",
                    "JWT_REALM",
                    "shop-api"
                ),
                expiresInSeconds = readString(
                    environment,
                    "app.jwt.expiresInSeconds",
                    "JWT_EXPIRES_IN_SECONDS",
                    "3600"
                ).toLong()
            ),
            bootstrap = BootstrapConfig(
                adminEmail = readString(
                    environment,
                    "app.bootstrap.adminEmail",
                    "ADMIN_EMAIL",
                    "admin@example.com"
                ),
                adminPassword = readString(
                    environment,
                    "app.bootstrap.adminPassword",
                    "ADMIN_PASSWORD",
                    "Admin123!"
                )
            )
        )
    }
}