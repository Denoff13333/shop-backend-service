@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.example.shop.app.repository

import com.example.shop.app.database.AuditLogsTable
import com.example.shop.app.database.DatabaseFactory
import com.example.shop.app.database.OrderItemsTable
import com.example.shop.app.database.OrdersTable
import com.example.shop.app.database.ProductsTable
import com.example.shop.app.domain.CreateOrderLine
import com.example.shop.app.domain.Order
import com.example.shop.app.domain.OrderAggregate
import com.example.shop.app.domain.OrderItem
import com.example.shop.app.domain.OrderStats
import com.example.shop.app.domain.TopProductStat
import com.example.shop.shared.model.OrderStatus
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.math.BigDecimal
import java.util.UUID

class ExposedOrderRepository(private val databaseFactory: DatabaseFactory) : OrderRepository {
    override suspend fun createOrder(userId: UUID, items: List<CreateOrderLine>): OrderAggregate =
        databaseFactory.dbQuery {
            val normalized = items
                .groupBy { it.productId }
                .map { (productId, lines) -> CreateOrderLine(productId, lines.sumOf { it.quantity }) }

            val productIds = normalized.map { it.productId.toKotlinUuid() }
            val productRows = ProductsTable
                .selectAll()
                .where { ProductsTable.id inList productIds }
                .toList()

            if (productRows.size != productIds.distinct().size) {
                throw NoSuchElementException("One or more products were not found")
            }

            val productsById = productRows.associateBy { it[ProductsTable.id].toJavaUuid() }

            normalized.forEach { line ->
                val stock = productsById.getValue(line.productId)[ProductsTable.stock]
                if (stock < line.quantity) {
                    throw IllegalStateException("Not enough stock for product ${line.productId}")
                }
            }

            val orderId = UUID.randomUUID()
            val now = System.currentTimeMillis()

            val total = normalized.fold(BigDecimal.ZERO) { acc, line ->
                val productRow = productsById.getValue(line.productId)
                val unitPrice = productRow[ProductsTable.price]
                acc.add(unitPrice.multiply(line.quantity.toBigDecimal()))
            }

            OrdersTable.insert {
                it[OrdersTable.id] = orderId.toKotlinUuid()
                it[OrdersTable.userId] = userId.toKotlinUuid()
                it[OrdersTable.status] = OrderStatus.CREATED.name
                it[OrdersTable.totalAmount] = total
                it[OrdersTable.createdAt] = now
                it[OrdersTable.updatedAt] = now
            }

            val createdItems = normalized.map { line ->
                val productRow = productsById.getValue(line.productId)
                val unitPrice = productRow[ProductsTable.price]

                ProductsTable.update({ ProductsTable.id eq line.productId.toKotlinUuid() }) {
                    it[ProductsTable.stock] = productRow[ProductsTable.stock] - line.quantity
                    it[ProductsTable.updatedAt] = now
                }

                val itemId = UUID.randomUUID()
                OrderItemsTable.insert {
                    it[OrderItemsTable.id] = itemId.toKotlinUuid()
                    it[OrderItemsTable.orderId] = orderId.toKotlinUuid()
                    it[OrderItemsTable.productId] = line.productId.toKotlinUuid()
                    it[OrderItemsTable.quantity] = line.quantity
                    it[OrderItemsTable.unitPrice] = unitPrice
                }

                OrderItem(
                    id = itemId,
                    orderId = orderId,
                    productId = line.productId,
                    productName = productRow[ProductsTable.name],
                    quantity = line.quantity,
                    unitPrice = unitPrice
                )
            }

            AuditLogsTable.insert {
                it[AuditLogsTable.id] = UUID.randomUUID().toKotlinUuid()
                it[AuditLogsTable.userId] = userId.toKotlinUuid()
                it[AuditLogsTable.action] = "ORDER_CREATED"
                it[AuditLogsTable.entityType] = "order"
                it[AuditLogsTable.entityId] = orderId.toKotlinUuid()
                it[AuditLogsTable.details] = "Order created with ${createdItems.size} item(s)"
                it[AuditLogsTable.createdAt] = now
            }

            OrderAggregate(
                order = Order(
                    id = orderId,
                    userId = userId,
                    status = OrderStatus.CREATED,
                    totalAmount = total,
                    createdAt = now,
                    updatedAt = now
                ),
                items = createdItems
            )
        }

    override suspend fun listUserOrders(userId: UUID): List<OrderAggregate> =
        databaseFactory.dbQuery {
            val orders = OrdersTable
                .selectAll()
                .where { OrdersTable.userId eq userId.toKotlinUuid() }
                .orderBy(OrdersTable.createdAt, SortOrder.DESC)
                .toList()

            loadAggregates(orders)
        }

    override suspend fun cancelOrder(userId: UUID, orderId: UUID): OrderAggregate =
        databaseFactory.dbQuery {
            val orderRow = OrdersTable
                .selectAll()
                .where {
                    (OrdersTable.id eq orderId.toKotlinUuid()) and
                        (OrdersTable.userId eq userId.toKotlinUuid())
                }
                .singleOrNull()
                ?: throw NoSuchElementException("Order was not found")

            if (orderRow[OrdersTable.status] == OrderStatus.CANCELLED.name) {
                throw IllegalStateException("Order is already cancelled")
            }

            val itemRows = OrderItemsTable
                .selectAll()
                .where { OrderItemsTable.orderId eq orderId.toKotlinUuid() }
                .toList()

            itemRows.forEach { item ->
                val productId = item[OrderItemsTable.productId].toJavaUuid()
                val quantity = item[OrderItemsTable.quantity]

                val productRow = ProductsTable
                    .selectAll()
                    .where { ProductsTable.id eq productId.toKotlinUuid() }
                    .single()

                ProductsTable.update({ ProductsTable.id eq productId.toKotlinUuid() }) {
                    it[ProductsTable.stock] = productRow[ProductsTable.stock] + quantity
                    it[ProductsTable.updatedAt] = System.currentTimeMillis()
                }
            }

            val now = System.currentTimeMillis()
            OrdersTable.update({ OrdersTable.id eq orderId.toKotlinUuid() }) {
                it[OrdersTable.status] = OrderStatus.CANCELLED.name
                it[OrdersTable.updatedAt] = now
            }

            AuditLogsTable.insert {
                it[AuditLogsTable.id] = UUID.randomUUID().toKotlinUuid()
                it[AuditLogsTable.userId] = userId.toKotlinUuid()
                it[AuditLogsTable.action] = "ORDER_CANCELLED"
                it[AuditLogsTable.entityType] = "order"
                it[AuditLogsTable.entityId] = orderId.toKotlinUuid()
                it[AuditLogsTable.details] = "Order cancelled by user"
                it[AuditLogsTable.createdAt] = now
            }

            val updatedOrderRow = OrdersTable
                .selectAll()
                .where { OrdersTable.id eq orderId.toKotlinUuid() }
                .single()

            loadAggregates(listOf(updatedOrderRow)).single()
        }

    override suspend fun findById(orderId: UUID): OrderAggregate? =
        databaseFactory.dbQuery {
            val orderRow = OrdersTable
                .selectAll()
                .where { OrdersTable.id eq orderId.toKotlinUuid() }
                .singleOrNull()
                ?: return@dbQuery null

            loadAggregates(listOf(orderRow)).single()
        }

    override suspend fun getStats(): OrderStats =
        databaseFactory.dbQuery {
            val orders = OrdersTable.selectAll().toList()
            val totalOrders = orders.size.toLong()
            val cancelledOrders = orders.count { it[OrdersTable.status] == OrderStatus.CANCELLED.name }.toLong()
            val totalRevenue = orders
                .filter { it[OrdersTable.status] == OrderStatus.CREATED.name }
                .fold(BigDecimal.ZERO) { acc, row -> acc.add(row[OrdersTable.totalAmount]) }

            val itemRows = OrderItemsTable.selectAll().toList()
            val productIds = itemRows.map { it[OrderItemsTable.productId] }.distinct()
            val productRows = if (productIds.isEmpty()) {
                emptyList()
            } else {
                ProductsTable
                    .selectAll()
                    .where { ProductsTable.id inList productIds }
                    .toList()
            }

            val productNames = productRows.associate { it[ProductsTable.id] to it[ProductsTable.name] }

            val topProducts = itemRows
                .groupBy { it[OrderItemsTable.productId] }
                .map { (productId, rows) ->
                    TopProductStat(
                        productId = productId.toJavaUuid(),
                        productName = productNames[productId] ?: "Unknown product",
                        orderedQuantity = rows.sumOf { it[OrderItemsTable.quantity] }
                    )
                }
                .sortedByDescending { it.orderedQuantity }
                .take(5)

            OrderStats(
                totalOrders = totalOrders,
                cancelledOrders = cancelledOrders,
                totalRevenue = totalRevenue,
                topProducts = topProducts
            )
        }

    private fun loadAggregates(orderRows: List<ResultRow>): List<OrderAggregate> {
        if (orderRows.isEmpty()) return emptyList()

        val orderIds = orderRows.map { it[OrdersTable.id] }
        val itemRows = OrderItemsTable
            .selectAll()
            .where { OrderItemsTable.orderId inList orderIds }
            .toList()

        val productIds = itemRows.map { it[OrderItemsTable.productId] }.distinct()
        val productNameMap = if (productIds.isEmpty()) {
            emptyMap()
        } else {
            ProductsTable
                .selectAll()
                .where { ProductsTable.id inList productIds }
                .associate { it[ProductsTable.id] to it[ProductsTable.name] }
        }

        val itemsByOrderId = itemRows
            .map { row ->
                OrderItem(
                    id = row[OrderItemsTable.id].toJavaUuid(),
                    orderId = row[OrderItemsTable.orderId].toJavaUuid(),
                    productId = row[OrderItemsTable.productId].toJavaUuid(),
                    productName = productNameMap[row[OrderItemsTable.productId]] ?: "Unknown product",
                    quantity = row[OrderItemsTable.quantity],
                    unitPrice = row[OrderItemsTable.unitPrice]
                )
            }
            .groupBy { it.orderId }

        return orderRows.map { row ->
            OrderAggregate(
                order = Order(
                    id = row[OrdersTable.id].toJavaUuid(),
                    userId = row[OrdersTable.userId].toJavaUuid(),
                    status = OrderStatus.valueOf(row[OrdersTable.status]),
                    totalAmount = row[OrdersTable.totalAmount],
                    createdAt = row[OrdersTable.createdAt],
                    updatedAt = row[OrdersTable.updatedAt]
                ),
                items = itemsByOrderId[row[OrdersTable.id].toJavaUuid()].orEmpty()
            )
        }
    }
}