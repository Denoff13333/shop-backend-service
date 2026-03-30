package com.example.shop.app.cache

import com.example.shop.shared.dto.OrderResponse
import com.example.shop.shared.dto.ProductResponse

interface CacheService {
    suspend fun getProduct(productId: String): ProductResponse?
    suspend fun putProduct(product: ProductResponse)
    suspend fun evictProduct(productId: String)

    suspend fun getOrder(orderId: String): OrderResponse?
    suspend fun putOrder(order: OrderResponse)
    suspend fun evictOrder(orderId: String)
}
