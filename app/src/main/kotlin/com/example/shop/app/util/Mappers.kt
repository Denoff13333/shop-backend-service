package com.example.shop.app.util

import com.example.shop.app.domain.OrderAggregate
import com.example.shop.app.domain.OrderStats
import com.example.shop.app.domain.Product
import com.example.shop.app.domain.User
import com.example.shop.shared.dto.AuthResponse
import com.example.shop.shared.dto.OrderResponse
import com.example.shop.shared.dto.OrderItemResponse
import com.example.shop.shared.dto.OrderStatsResponse
import com.example.shop.shared.dto.ProductResponse
import com.example.shop.shared.dto.TopProductStatResponse
import com.example.shop.shared.dto.UserInfoResponse
import java.time.Instant
import java.util.UUID

fun User.toAuthResponse(token: String): AuthResponse =
    AuthResponse(
        accessToken = token,
        user = UserInfoResponse(
            id = id.toString(),
            email = email,
            role = role
        )
    )

fun Product.toResponse(): ProductResponse =
    ProductResponse(
        id = id.toString(),
        name = name,
        description = description,
        price = price.toPlainString(),
        stock = stock,
        createdAt = Instant.ofEpochMilli(createdAt).toString(),
        updatedAt = Instant.ofEpochMilli(updatedAt).toString()
    )

fun OrderAggregate.toResponse(): OrderResponse =
    OrderResponse(
        id = order.id.toString(),
        userId = order.userId.toString(),
        status = order.status,
        totalAmount = order.totalAmount.toPlainString(),
        items = items.map {
            OrderItemResponse(
                id = it.id.toString(),
                productId = it.productId.toString(),
                productName = it.productName,
                quantity = it.quantity,
                unitPrice = it.unitPrice.toPlainString(),
                lineTotal = it.unitPrice.multiply(it.quantity.toBigDecimal()).toPlainString()
            )
        },
        createdAt = Instant.ofEpochMilli(order.createdAt).toString(),
        updatedAt = Instant.ofEpochMilli(order.updatedAt).toString()
    )

fun OrderStats.toResponse(): OrderStatsResponse =
    OrderStatsResponse(
        totalOrders = totalOrders,
        cancelledOrders = cancelledOrders,
        totalRevenue = totalRevenue.toPlainString(),
        topProducts = topProducts.map {
            TopProductStatResponse(
                productId = it.productId.toString(),
                productName = it.productName,
                orderedQuantity = it.orderedQuantity
            )
        }
    )

fun parseUuid(raw: String, fieldName: String): UUID =
    try {
        UUID.fromString(raw)
    } catch (_: IllegalArgumentException) {
        throw IllegalArgumentException("Invalid UUID for $fieldName")
    }
