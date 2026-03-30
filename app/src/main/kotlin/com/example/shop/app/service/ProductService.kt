package com.example.shop.app.service

import com.example.shop.app.cache.CacheService
import com.example.shop.app.exceptions.ConflictException
import com.example.shop.app.exceptions.NotFoundException
import com.example.shop.app.exceptions.ValidationException
import com.example.shop.app.repository.ProductRepository
import com.example.shop.app.util.toResponse
import com.example.shop.shared.dto.ProductResponse
import java.util.UUID

class ProductService(
    private val productRepository: ProductRepository,
    private val cacheService: CacheService
) {
    suspend fun listProducts(): List<ProductResponse> =
        productRepository.findAll().map { it.toResponse() }

    suspend fun getProduct(id: String): ProductResponse {
        cacheService.getProduct(id)?.let { return it }

        val uuid = parseUuid(id)
        val product = productRepository.findById(uuid)
            ?: throw NotFoundException("Product $id was not found")

        return product.toResponse().also { cacheService.putProduct(it) }
    }

    suspend fun createProduct(name: String, description: String, price: String, stock: Int): ProductResponse {
        validateProduct(name, description, price, stock)
        return productRepository.create(name, description, price, stock).toResponse()
    }

    suspend fun updateProduct(id: String, name: String, description: String, price: String, stock: Int): ProductResponse {
        validateProduct(name, description, price, stock)
        val updated = productRepository.update(parseUuid(id), name, description, price, stock)
            ?: throw NotFoundException("Product $id was not found")

        val response = updated.toResponse()
        cacheService.evictProduct(id)
        cacheService.putProduct(response)
        return response
    }

    suspend fun deleteProduct(id: String) {
        val uuid = parseUuid(id)
        val existing = productRepository.findById(uuid)
            ?: throw NotFoundException("Product $id was not found")

        val deleted = productRepository.delete(existing.id)
        if (!deleted) {
            throw ConflictException("Product cannot be deleted because it is used in existing orders")
        }
        cacheService.evictProduct(id)
    }

    private fun validateProduct(name: String, description: String, price: String, stock: Int) {
        if (name.isBlank()) throw ValidationException("Product name is required")
        if (description.isBlank()) throw ValidationException("Product description is required")
        price.toBigDecimalOrNull() ?: throw ValidationException("Price must be a valid decimal number")
        if (stock < 0) throw ValidationException("Stock cannot be negative")
    }

    private fun parseUuid(id: String): UUID =
        runCatching { UUID.fromString(id) }
            .getOrElse { throw ValidationException("Invalid product id: $id") }
}
