package com.example.shop.app.unit

import com.example.shop.app.service.OrderService
import com.example.shop.app.support.InMemoryCacheService
import com.example.shop.app.support.InMemoryOrderRepository
import com.example.shop.app.support.InMemoryUserRepository
import com.example.shop.app.support.RecordingPublisher
import com.example.shop.shared.dto.CreateOrderItemRequest
import com.example.shop.shared.dto.CreateOrderRequest
import com.example.shop.shared.model.UserRole
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OrderServiceTest {
    @Test
    fun `createOrder publishes event and caches order`() = runTest {
        val userRepository = InMemoryUserRepository()
        val orderRepository = InMemoryOrderRepository()
        val cache = InMemoryCacheService()
        val publisher = RecordingPublisher()
        val service = OrderService(orderRepository, userRepository, cache, publisher)

        val user = userRepository.create("user@example.com", "hash", UserRole.USER)

        val response = service.createOrder(
            userId = user.id,
            request = CreateOrderRequest(
                items = listOf(
                    CreateOrderItemRequest(productId = java.util.UUID.randomUUID().toString(), quantity = 2)
                )
            )
        )

        assertEquals(response.id, cache.orders.keys.single())
        assertEquals("ORDER_CREATED", publisher.messages.single().eventType)
        assertTrue(response.totalAmount.isNotBlank())
    }
}
