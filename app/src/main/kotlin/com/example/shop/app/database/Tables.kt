@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.example.shop.app.database

import org.jetbrains.exposed.v1.core.Table

object UsersTable : Table("users") {
    val id = uuid("id")
    val email = varchar("email", 255)
    val passwordHash = varchar("password_hash", 255)
    val role = varchar("role", 20)
    val createdAt = long("created_at")

    override val primaryKey = PrimaryKey(id)
}

object ProductsTable : Table("products") {
    val id = uuid("id")
    val name = varchar("name", 255)
    val description = text("description")
    val price = decimal("price", 19, 2)
    val stock = integer("stock")
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")

    override val primaryKey = PrimaryKey(id)
}

object OrdersTable : Table("orders") {
    val id = uuid("id")
    val userId = uuid("user_id").references(UsersTable.id)
    val status = varchar("status", 30)
    val totalAmount = decimal("total_amount", 19, 2)
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")

    override val primaryKey = PrimaryKey(id)
}

object OrderItemsTable : Table("order_items") {
    val id = uuid("id")
    val orderId = uuid("order_id").references(OrdersTable.id)
    val productId = uuid("product_id").references(ProductsTable.id)
    val quantity = integer("quantity")
    val unitPrice = decimal("unit_price", 19, 2)

    override val primaryKey = PrimaryKey(id)
}

object AuditLogsTable : Table("audit_logs") {
    val id = uuid("id")
    val userId = uuid("user_id").references(UsersTable.id).nullable()
    val action = varchar("action", 100)
    val entityType = varchar("entity_type", 100)
    val entityId = uuid("entity_id")
    val details = text("details").nullable()
    val createdAt = long("created_at")

    override val primaryKey = PrimaryKey(id)
}
