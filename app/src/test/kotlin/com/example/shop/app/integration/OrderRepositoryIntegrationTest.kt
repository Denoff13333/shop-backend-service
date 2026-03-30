package com.example.shop.app.integration

import com.example.shop.app.domain.CreateOrderLine
import com.example.shop.app.repository.ExposedOrderRepository
import com.example.shop.app.repository.ExposedProductRepository
import com.example.shop.app.repository.ExposedUserRepository
import com.example.shop.shared.model.OrderStatus
import com.example.shop.shared.model.UserRole
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class OrderRepositoryIntegrationTest : PostgresIntegrationTestSupport() {
    @Test
    fun `create and cancel order updates stock and status`() = runBlocking {
        val userRepository = ExposedUserRepository(databaseFactory)
        val productRepository = ExposedProductRepository(databaseFactory)
        val orderRepository = ExposedOrderRepository(databaseFactory)

        val user = userRepository.create("buyer@example.com", "hash", UserRole.USER)
        val product = productRepository.create("Monitor", "4K", "350.00", 7)

        val created = orderRepository.createOrder(
            userId = user.id,
            items = listOf(CreateOrderLine(product.id, 2))
        )

        assertEquals("5", productRepository.findById(product.id)?.stock.toString())
        assertEquals(OrderStatus.CREATED, created.order.status)

        val cancelled = orderRepository.cancelOrder(user.id, created.order.id)

        assertEquals(OrderStatus.CANCELLED, cancelled.order.status)
        assertEquals("7", productRepository.findById(product.id)?.stock.toString())
    }
}
