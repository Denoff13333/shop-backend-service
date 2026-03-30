package com.example.shop.app.service

import com.example.shop.app.cache.CacheService
import com.example.shop.app.domain.CreateOrderLine
import com.example.shop.app.exceptions.BusinessRuleException
import com.example.shop.app.exceptions.NotFoundException
import com.example.shop.app.exceptions.ValidationException
import com.example.shop.app.messaging.OrderEventPublisher
import com.example.shop.app.repository.OrderRepository
import com.example.shop.app.repository.UserRepository
import com.example.shop.app.util.toResponse
import com.example.shop.shared.dto.CreateOrderRequest
import com.example.shop.shared.dto.OrderResponse
import com.example.shop.shared.events.OrderEventMessage
import java.time.Instant
import java.util.UUID

class OrderService(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository,
    private val cacheService: CacheService,
    private val publisher: OrderEventPublisher
) {
    suspend fun createOrder(userId: UUID, request: CreateOrderRequest): OrderResponse {
        if (request.items.isEmpty()) {
            throw ValidationException("Order must contain at least one item")
        }

        val lines = request.items.map { item ->
            if (item.quantity <= 0) throw ValidationException("Quantity must be positive")
            val productId = runCatching { UUID.fromString(item.productId) }
                .getOrElse { throw ValidationException("Invalid product id: ${item.productId}") }
            CreateOrderLine(productId = productId, quantity = item.quantity)
        }

        val aggregate = try {
            orderRepository.createOrder(userId, lines)
        } catch (ex: NoSuchElementException) {
            throw NotFoundException(ex.message ?: "Product was not found")
        } catch (ex: IllegalStateException) {
            throw BusinessRuleException(ex.message ?: "Order cannot be created")
        }

        val response = aggregate.toResponse()
        cacheService.putOrder(response)

        val user = userRepository.findById(userId)
            ?: throw NotFoundException("User was not found")

        publisher.publish(
            OrderEventMessage(
                eventType = "ORDER_CREATED",
                orderId = response.id,
                userId = userId.toString(),
                userEmail = user.email,
                totalAmount = response.totalAmount,
                createdAt = Instant.now().toString()
            )
        )

        return response
    }

    suspend fun listOrders(userId: UUID): List<OrderResponse> =
        orderRepository.listUserOrders(userId).map { it.toResponse() }

    suspend fun cancelOrder(userId: UUID, orderId: UUID): OrderResponse {
        val aggregate = try {
            orderRepository.cancelOrder(userId, orderId)
        } catch (ex: NoSuchElementException) {
            throw NotFoundException(ex.message ?: "Order was not found")
        } catch (ex: IllegalStateException) {
            throw BusinessRuleException(ex.message ?: "Order cannot be cancelled")
        }

        val response = aggregate.toResponse()
        cacheService.putOrder(response)

        val user = userRepository.findById(userId)
            ?: throw NotFoundException("User was not found")

        publisher.publish(
            OrderEventMessage(
                eventType = "ORDER_CANCELLED",
                orderId = response.id,
                userId = userId.toString(),
                userEmail = user.email,
                totalAmount = response.totalAmount,
                createdAt = Instant.now().toString()
            )
        )

        return response
    }
}
