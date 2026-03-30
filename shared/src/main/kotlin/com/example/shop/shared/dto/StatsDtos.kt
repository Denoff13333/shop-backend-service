package com.example.shop.shared.dto

import kotlinx.serialization.Serializable

@Serializable
data class TopProductStatResponse(
    val productId: String,
    val productName: String,
    val orderedQuantity: Int
)

@Serializable
data class OrderStatsResponse(
    val totalOrders: Long,
    val cancelledOrders: Long,
    val totalRevenue: String,
    val topProducts: List<TopProductStatResponse>
)
