package com.example.shop.app.repository

import com.example.shop.app.domain.CreateOrderLine
import com.example.shop.app.domain.OrderAggregate
import com.example.shop.app.domain.OrderStats
import com.example.shop.app.domain.Product
import com.example.shop.app.domain.User
import com.example.shop.shared.model.UserRole
import java.util.UUID

interface UserRepository {
    suspend fun create(email: String, passwordHash: String, role: UserRole): User
    suspend fun findByEmail(email: String): User?
    suspend fun findById(id: UUID): User?
    suspend fun upsertAdmin(email: String, passwordHash: String): User
}

interface ProductRepository {
    suspend fun findAll(): List<Product>
    suspend fun findById(id: UUID): Product?
    suspend fun create(name: String, description: String, price: String, stock: Int): Product
    suspend fun update(id: UUID, name: String, description: String, price: String, stock: Int): Product?
    suspend fun delete(id: UUID): Boolean
}

interface OrderRepository {
    suspend fun createOrder(userId: UUID, items: List<CreateOrderLine>): OrderAggregate
    suspend fun listUserOrders(userId: UUID): List<OrderAggregate>
    suspend fun cancelOrder(userId: UUID, orderId: UUID): OrderAggregate
    suspend fun findById(orderId: UUID): OrderAggregate?
    suspend fun getStats(): OrderStats
}
