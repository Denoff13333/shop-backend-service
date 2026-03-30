package com.example.shop.shared.dto

import com.example.shop.shared.model.OrderStatus
import kotlinx.serialization.Serializable

@Serializable
data class CreateOrderRequest(
    val items: List<CreateOrderItemRequest>
)

@Serializable
data class CreateOrderItemRequest(
    val productId: String,
    val quantity: Int
)

@Serializable
data class OrderItemResponse(
    val id: String,
    val productId: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: String,
    val lineTotal: String
)

@Serializable
data class OrderResponse(
    val id: String,
    val userId: String,
    val status: OrderStatus,
    val totalAmount: String,
    val items: List<OrderItemResponse>,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class OrderListResponse(
    val items: List<OrderResponse>
)
