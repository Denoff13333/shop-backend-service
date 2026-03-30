package com.example.shop.app.messaging

import com.example.shop.app.config.RabbitMqConfig
import com.example.shop.shared.events.OrderEventMessage
import com.rabbitmq.client.ConnectionFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class RabbitMqOrderEventPublisher(
    private val config: RabbitMqConfig,
    private val json: Json = Json
) : OrderEventPublisher {
    private val factory = ConnectionFactory().apply {
        host = config.host
        port = config.port
        username = config.username
        password = config.password
    }

    override suspend fun publish(message: OrderEventMessage) {
        withContext(Dispatchers.IO) {
            runCatching {
                factory.newConnection().use { connection ->
                    connection.createChannel().use { channel ->
                        channel.queueDeclare(config.queue, true, false, false, null)
                        channel.basicPublish(
                            "",
                            config.queue,
                            null,
                            json.encodeToString(OrderEventMessage.serializer(), message).toByteArray()
                        )
                    }
                }
            }
        }
    }
}
