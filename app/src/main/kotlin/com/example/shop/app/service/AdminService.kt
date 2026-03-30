package com.example.shop.app.service

import com.example.shop.app.repository.OrderRepository
import com.example.shop.app.util.toResponse
import com.example.shop.shared.dto.OrderStatsResponse

class AdminService(
    private val orderRepository: OrderRepository
) {
    suspend fun getOrderStats(): OrderStatsResponse =
        orderRepository.getStats().toResponse()
}
