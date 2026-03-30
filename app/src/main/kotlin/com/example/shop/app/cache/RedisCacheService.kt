package com.example.shop.app.cache

import com.example.shop.shared.dto.OrderResponse
import com.example.shop.shared.dto.ProductResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import redis.clients.jedis.JedisPooled

class RedisCacheService(
    host: String,
    port: Int,
    private val ttlSeconds: Long,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : CacheService {
    private val jedis = JedisPooled(host, port)

    override suspend fun getProduct(productId: String): ProductResponse? =
        withContext(Dispatchers.IO) {
            runCatching {
                jedis.get(productKey(productId))?.let { json.decodeFromString(ProductResponse.serializer(), it) }
            }.getOrNull()
        }

    override suspend fun putProduct(product: ProductResponse) {
        withContext(Dispatchers.IO) {
            runCatching {
                jedis.setex(productKey(product.id), ttlSeconds, json.encodeToString(ProductResponse.serializer(), product))
            }
        }
    }

    override suspend fun evictProduct(productId: String) {
        withContext(Dispatchers.IO) {
            runCatching { jedis.del(productKey(productId)) }
        }
    }

    override suspend fun getOrder(orderId: String): OrderResponse? =
        withContext(Dispatchers.IO) {
            runCatching {
                jedis.get(orderKey(orderId))?.let { json.decodeFromString(OrderResponse.serializer(), it) }
            }.getOrNull()
        }

    override suspend fun putOrder(order: OrderResponse) {
        withContext(Dispatchers.IO) {
            runCatching {
                jedis.setex(orderKey(order.id), ttlSeconds, json.encodeToString(OrderResponse.serializer(), order))
            }
        }
    }

    override suspend fun evictOrder(orderId: String) {
        withContext(Dispatchers.IO) {
            runCatching { jedis.del(orderKey(orderId)) }
        }
    }

    private fun productKey(productId: String): String = "product:$productId"
    private fun orderKey(orderId: String): String = "order:$orderId"
}
