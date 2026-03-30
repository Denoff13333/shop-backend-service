package com.example.shop.app.bootstrap

import com.example.shop.app.cache.CacheService
import com.example.shop.app.cache.RedisCacheService
import com.example.shop.app.config.AppConfig
import com.example.shop.app.config.AppConfigLoader
import com.example.shop.app.database.DatabaseFactory
import com.example.shop.app.messaging.OrderEventPublisher
import com.example.shop.app.messaging.RabbitMqOrderEventPublisher
import com.example.shop.app.repository.ExposedOrderRepository
import com.example.shop.app.repository.ExposedProductRepository
import com.example.shop.app.repository.ExposedUserRepository
import com.example.shop.app.repository.OrderRepository
import com.example.shop.app.repository.ProductRepository
import com.example.shop.app.repository.UserRepository
import com.example.shop.app.security.BCryptPasswordHasher
import com.example.shop.app.security.JwtTokenProvider
import com.example.shop.app.security.PasswordHasher
import com.example.shop.app.service.AdminService
import com.example.shop.app.service.AuthService
import com.example.shop.app.service.OrderService
import com.example.shop.app.service.ProductService
import io.ktor.server.application.Application
import kotlinx.serialization.json.Json

data class AppDependencies(
    val config: AppConfig,
    val databaseFactory: DatabaseFactory,
    val userRepository: UserRepository,
    val productRepository: ProductRepository,
    val orderRepository: OrderRepository,
    val passwordHasher: PasswordHasher,
    val tokenProvider: JwtTokenProvider,
    val cacheService: CacheService,
    val orderEventPublisher: OrderEventPublisher,
    val authService: AuthService,
    val productService: ProductService,
    val orderService: OrderService,
    val adminService: AdminService
) {
    companion object {
        fun live(application: Application): AppDependencies {
            val config = AppConfigLoader.load(application.environment)
            val databaseFactory = DatabaseFactory(config.database)
            databaseFactory.init(migrate = true)

            val userRepository = ExposedUserRepository(databaseFactory)
            val productRepository = ExposedProductRepository(databaseFactory)
            val orderRepository = ExposedOrderRepository(databaseFactory)
            val passwordHasher = BCryptPasswordHasher()
            val tokenProvider = JwtTokenProvider(config.jwt)
            val cacheService = RedisCacheService(
                host = config.redis.host,
                port = config.redis.port,
                ttlSeconds = config.redis.ttlSeconds,
                json = Json { ignoreUnknownKeys = true }
            )
            val publisher = RabbitMqOrderEventPublisher(config.rabbitMq)

            val authService = AuthService(userRepository, passwordHasher, tokenProvider)
            val productService = ProductService(productRepository, cacheService)
            val orderService = OrderService(orderRepository, userRepository, cacheService, publisher)
            val adminService = AdminService(orderRepository)

            return AppDependencies(
                config = config,
                databaseFactory = databaseFactory,
                userRepository = userRepository,
                productRepository = productRepository,
                orderRepository = orderRepository,
                passwordHasher = passwordHasher,
                tokenProvider = tokenProvider,
                cacheService = cacheService,
                orderEventPublisher = publisher,
                authService = authService,
                productService = productService,
                orderService = orderService,
                adminService = adminService
            )
        }
    }
}
