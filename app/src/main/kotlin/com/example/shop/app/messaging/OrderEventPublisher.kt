package com.example.shop.app.messaging

import com.example.shop.shared.events.OrderEventMessage

interface OrderEventPublisher {
    suspend fun publish(message: OrderEventMessage)
}
