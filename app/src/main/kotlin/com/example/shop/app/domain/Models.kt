package com.example.shop.app.domain

import com.example.shop.shared.model.OrderStatus
import com.example.shop.shared.model.UserRole
import java.math.BigDecimal
import java.util.UUID

data class User(
    val id: UUID,
    val email: String,
    val passwordHash: String,
    val role: UserRole,
    val createdAt: Long
)

data class Product(
    val id: UUID,
    val name: String,
    val description: String,
    val price: BigDecimal,
    val stock: Int,
    val createdAt: Long,
    val updatedAt: Long
)

data class Order(
    val id: UUID,
    val userId: UUID,
    val status: OrderStatus,
    val totalAmount: BigDecimal,
    val createdAt: Long,
    val updatedAt: Long
)

data class OrderItem(
    val id: UUID,
    val orderId: UUID,
    val productId: UUID,
    val productName: String,
    val quantity: Int,
    val unitPrice: BigDecimal
)

data class AuditLog(
    val id: UUID,
    val userId: UUID?,
    val action: String,
    val entityType: String,
    val entityId: UUID,
    val details: String?,
    val createdAt: Long
)

data class TopProductStat(
    val productId: UUID,
    val productName: String,
    val orderedQuantity: Int
)

data class OrderStats(
    val totalOrders: Long,
    val cancelledOrders: Long,
    val totalRevenue: BigDecimal,
    val topProducts: List<TopProductStat>
)

data class OrderAggregate(
    val order: Order,
    val items: List<OrderItem>
)

data class CreateOrderLine(
    val productId: UUID,
    val quantity: Int
)
