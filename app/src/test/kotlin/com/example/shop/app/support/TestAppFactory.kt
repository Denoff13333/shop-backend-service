package com.example.shop.app.support

import com.example.shop.app.bootstrap.AppDependencies
import com.example.shop.app.config.AppConfig
import com.example.shop.app.config.BootstrapConfig
import com.example.shop.app.config.DatabaseConfig
import com.example.shop.app.config.JwtConfig
import com.example.shop.app.config.RabbitMqConfig
import com.example.shop.app.config.RedisConfig
import com.example.shop.app.database.DatabaseFactory
import com.example.shop.app.security.BCryptPasswordHasher
import com.example.shop.app.security.JwtTokenProvider
import com.example.shop.app.service.AdminService
import com.example.shop.app.service.AuthService
import com.example.shop.app.service.OrderService
import com.example.shop.app.service.ProductService

object TestAppFactory {
    fun create(
        userRepository: InMemoryUserRepository = InMemoryUserRepository(),
        productRepository: InMemoryProductRepository = InMemoryProductRepository(),
        orderRepository: InMemoryOrderRepository = InMemoryOrderRepository(),
        cacheService: InMemoryCacheService = InMemoryCacheService(),
        publisher: RecordingPublisher = RecordingPublisher()
    ): AppDependencies {
        val config = AppConfig(
            database = DatabaseConfig("jdbc:postgresql://localhost/test", "test", "test", "org.postgresql.Driver", 1),
            redis = RedisConfig("localhost", 6379, 60),
            rabbitMq = RabbitMqConfig("localhost", 5672, "guest", "guest", "order-events"),
            jwt = JwtConfig("test-secret", "shop-service", "shop-users", "shop-api", 3600),
            bootstrap = BootstrapConfig("admin@example.com", "Admin123!")
        )

        val hasher = BCryptPasswordHasher()
        val tokenProvider = JwtTokenProvider(config.jwt)
        val authService = AuthService(userRepository, hasher, tokenProvider)
        val productService = ProductService(productRepository, cacheService)
        val orderService = OrderService(orderRepository, userRepository, cacheService, publisher)
        val adminService = AdminService(orderRepository)

        return AppDependencies(
            config = config,
            databaseFactory = DatabaseFactory(config.database),
            userRepository = userRepository,
            productRepository = productRepository,
            orderRepository = orderRepository,
            passwordHasher = hasher,
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
