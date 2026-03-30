package com.example.shop.worker

import com.example.shop.shared.events.OrderEventMessage
import com.rabbitmq.client.ConnectionFactory
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

fun main() {
    val logger = LoggerFactory.getLogger("OrderEventWorker")
    val host = System.getenv("RABBITMQ_HOST") ?: "localhost"
    val port = (System.getenv("RABBITMQ_PORT") ?: "5672").toInt()
    val username = System.getenv("RABBITMQ_USERNAME") ?: "guest"
    val password = System.getenv("RABBITMQ_PASSWORD") ?: "guest"
    val queue = System.getenv("RABBITMQ_QUEUE") ?: "order-events"

    val factory = ConnectionFactory().apply {
        this.host = host
        this.port = port
        this.username = username
        this.password = password
    }

    logger.info("Starting worker. Listening queue='{}' on {}:{}", queue, host, port)

    factory.newConnection().use { connection ->
        connection.createChannel().use { channel ->
            channel.queueDeclare(queue, true, false, false, null)

            val deliverCallback = com.rabbitmq.client.DeliverCallback { _, delivery ->
                val payload = String(delivery.body)
                runCatching {
                    val message = Json.decodeFromString(OrderEventMessage.serializer(), payload)
                    logger.info(
                        "Received event={} orderId={} userId={} amount={}",
                        message.eventType,
                        message.orderId,
                        message.userId,
                        message.totalAmount
                    )
                    logger.info("Fake email sent to {}", message.userEmail)
                }.onFailure { ex ->
                    logger.error("Failed to process message: {}", payload, ex)
                }
            }

            channel.basicConsume(queue, true, deliverCallback) { consumerTag ->
                logger.info("Consumer {} was cancelled", consumerTag)
            }

            while (true) {
                Thread.sleep(1000)
            }
        }
    }
}
