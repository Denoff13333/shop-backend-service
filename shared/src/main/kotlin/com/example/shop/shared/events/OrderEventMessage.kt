package com.example.shop.shared.events

import kotlinx.serialization.Serializable

@Serializable
data class OrderEventMessage(
    val eventType: String,
    val orderId: String,
    val userId: String,
    val userEmail: String,
    val totalAmount: String,
    val createdAt: String
)
