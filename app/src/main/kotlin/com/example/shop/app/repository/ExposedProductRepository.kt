@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.example.shop.app.repository

import com.example.shop.app.database.DatabaseFactory
import com.example.shop.app.database.OrderItemsTable
import com.example.shop.app.database.ProductsTable
import com.example.shop.app.domain.Product
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.math.BigDecimal
import java.util.UUID

class ExposedProductRepository(private val databaseFactory: DatabaseFactory) : ProductRepository {
    override suspend fun findAll(): List<Product> =
        databaseFactory.dbQuery {
            ProductsTable
                .selectAll()
                .orderBy(ProductsTable.createdAt, SortOrder.DESC)
                .map(::mapRow)
        }

    override suspend fun findById(id: UUID): Product? =
        databaseFactory.dbQuery {
            ProductsTable
                .selectAll()
                .where { ProductsTable.id eq id.toKotlinUuid() }
                .singleOrNull()
                ?.let(::mapRow)
        }

    override suspend fun create(name: String, description: String, price: String, stock: Int): Product =
        databaseFactory.dbQuery {
            val id = UUID.randomUUID()
            val now = System.currentTimeMillis()
            val decimalPrice = BigDecimal(price)

            ProductsTable.insert {
                it[ProductsTable.id] = id.toKotlinUuid()
                it[ProductsTable.name] = name
                it[ProductsTable.description] = description
                it[ProductsTable.price] = decimalPrice
                it[ProductsTable.stock] = stock
                it[ProductsTable.createdAt] = now
                it[ProductsTable.updatedAt] = now
            }

            Product(
                id = id,
                name = name,
                description = description,
                price = decimalPrice,
                stock = stock,
                createdAt = now,
                updatedAt = now
            )
        }

    override suspend fun update(id: UUID, name: String, description: String, price: String, stock: Int): Product? =
        databaseFactory.dbQuery {
            val now = System.currentTimeMillis()
            val updatedRows = ProductsTable.update({ ProductsTable.id eq id.toKotlinUuid() }) {
                it[ProductsTable.name] = name
                it[ProductsTable.description] = description
                it[ProductsTable.price] = BigDecimal(price)
                it[ProductsTable.stock] = stock
                it[ProductsTable.updatedAt] = now
            }

            if (updatedRows == 0) {
                null
            } else {
                ProductsTable
                    .selectAll()
                    .where { ProductsTable.id eq id.toKotlinUuid() }
                    .singleOrNull()
                    ?.let(::mapRow)
            }
        }

    override suspend fun delete(id: UUID): Boolean =
        databaseFactory.dbQuery {
            val referenced = OrderItemsTable
                .selectAll()
                .where { OrderItemsTable.productId eq id.toKotlinUuid() }
                .count() > 0

            if (referenced) {
                false
            } else {
                ProductsTable.deleteWhere { ProductsTable.id eq id.toKotlinUuid() } > 0
            }
        }

    private fun mapRow(row: ResultRow): Product =
        Product(
            id = row[ProductsTable.id].toJavaUuid(),
            name = row[ProductsTable.name],
            description = row[ProductsTable.description],
            price = row[ProductsTable.price],
            stock = row[ProductsTable.stock],
            createdAt = row[ProductsTable.createdAt],
            updatedAt = row[ProductsTable.updatedAt]
        )
}
