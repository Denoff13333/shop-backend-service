package com.example.shop.app.support

import com.example.shop.app.cache.CacheService
import com.example.shop.app.domain.CreateOrderLine
import com.example.shop.app.domain.Order
import com.example.shop.app.domain.OrderAggregate
import com.example.shop.app.domain.OrderItem
import com.example.shop.app.domain.OrderStats
import com.example.shop.app.domain.Product
import com.example.shop.app.domain.TopProductStat
import com.example.shop.app.domain.User
import com.example.shop.app.messaging.OrderEventPublisher
import com.example.shop.app.repository.OrderRepository
import com.example.shop.app.repository.ProductRepository
import com.example.shop.app.repository.UserRepository
import com.example.shop.shared.dto.OrderResponse
import com.example.shop.shared.dto.ProductResponse
import com.example.shop.shared.events.OrderEventMessage
import com.example.shop.shared.model.OrderStatus
import com.example.shop.shared.model.UserRole
import java.math.BigDecimal
import java.util.UUID

class InMemoryUserRepository : UserRepository {
    private val users = mutableListOf<User>()

    override suspend fun create(email: String, passwordHash: String, role: UserRole): User {
        val user = User(UUID.randomUUID(), email, passwordHash, role, System.currentTimeMillis())
        users += user
        return user
    }

    override suspend fun findByEmail(email: String): User? = users.find { it.email == email }

    override suspend fun findById(id: UUID): User? = users.find { it.id == id }

    override suspend fun upsertAdmin(email: String, passwordHash: String): User {
        val existing = users.find { it.email == email }
        return if (existing != null) {
            users.remove(existing)
            val admin = existing.copy(passwordHash = passwordHash, role = UserRole.ADMIN)
            users += admin
            admin
        } else {
            create(email, passwordHash, UserRole.ADMIN)
        }
    }
}

class InMemoryProductRepository : ProductRepository {
    val products = mutableMapOf<UUID, Product>()

    override suspend fun findAll(): List<Product> = products.values.sortedByDescending { it.createdAt }

    override suspend fun findById(id: UUID): Product? = products[id]

    override suspend fun create(name: String, description: String, price: String, stock: Int): Product {
        val now = System.currentTimeMillis()
        val product = Product(UUID.randomUUID(), name, description, BigDecimal(price), stock, now, now)
        products[product.id] = product
        return product
    }

    override suspend fun update(id: UUID, name: String, description: String, price: String, stock: Int): Product? {
        val existing = products[id] ?: return null
        val updated = existing.copy(
            name = name,
            description = description,
            price = BigDecimal(price),
            stock = stock,
            updatedAt = System.currentTimeMillis()
        )
        products[id] = updated
        return updated
    }

    override suspend fun delete(id: UUID): Boolean = products.remove(id) != null
}

class InMemoryOrderRepository : OrderRepository {
    val orders = mutableMapOf<UUID, OrderAggregate>()

    override suspend fun createOrder(userId: UUID, items: List<CreateOrderLine>): OrderAggregate {
        val now = System.currentTimeMillis()
        val orderId = UUID.randomUUID()
        val orderItems = items.mapIndexed { index, line ->
            OrderItem(
                id = UUID.randomUUID(),
                orderId = orderId,
                productId = line.productId,
                productName = "Product-${index + 1}",
                quantity = line.quantity,
                unitPrice = BigDecimal("10.00")
            )
        }
        val total = orderItems.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.unitPrice.multiply(item.quantity.toBigDecimal())) }
        val aggregate = OrderAggregate(
            order = Order(orderId, userId, OrderStatus.CREATED, total, now, now),
            items = orderItems
        )
        orders[orderId] = aggregate
        return aggregate
    }

    override suspend fun listUserOrders(userId: UUID): List<OrderAggregate> =
        orders.values.filter { it.order.userId == userId }

    override suspend fun cancelOrder(userId: UUID, orderId: UUID): OrderAggregate {
        val existing = orders[orderId] ?: throw NoSuchElementException("Order was not found")
        if (existing.order.userId != userId) throw NoSuchElementException("Order was not found")
        val updated = existing.copy(order = existing.order.copy(status = OrderStatus.CANCELLED, updatedAt = System.currentTimeMillis()))
        orders[orderId] = updated
        return updated
    }

    override suspend fun findById(orderId: UUID): OrderAggregate? = orders[orderId]

    override suspend fun getStats(): OrderStats =
        OrderStats(
            totalOrders = orders.size.toLong(),
            cancelledOrders = orders.values.count { it.order.status == OrderStatus.CANCELLED }.toLong(),
            totalRevenue = orders.values.fold(BigDecimal.ZERO) { acc, order -> acc.add(order.order.totalAmount) },
            topProducts = listOf(
                TopProductStat(UUID.randomUUID(), "Demo product", 5)
            )
        )
}

class InMemoryCacheService : CacheService {
    val products = mutableMapOf<String, ProductResponse>()
    val orders = mutableMapOf<String, OrderResponse>()

    override suspend fun getProduct(productId: String): ProductResponse? = products[productId]

    override suspend fun putProduct(product: ProductResponse) {
        products[product.id] = product
    }

    override suspend fun evictProduct(productId: String) {
        products.remove(productId)
    }

    override suspend fun getOrder(orderId: String): OrderResponse? = orders[orderId]

    override suspend fun putOrder(order: OrderResponse) {
        orders[order.id] = order
    }

    override suspend fun evictOrder(orderId: String) {
        orders.remove(orderId)
    }
}

class RecordingPublisher : OrderEventPublisher {
    val messages = mutableListOf<OrderEventMessage>()

    override suspend fun publish(message: OrderEventMessage) {
        messages += message
    }
}
