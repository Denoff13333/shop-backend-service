package com.example.shop.app.unit

import com.example.shop.app.service.ProductService
import com.example.shop.app.support.InMemoryCacheService
import com.example.shop.app.support.InMemoryProductRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ProductServiceTest {
    @Test
    fun `getProduct returns cached value when present`() = runTest {
        val repository = InMemoryProductRepository()
        val cache = InMemoryCacheService()
        val service = ProductService(repository, cache)

        val created = repository.create("Mouse", "Wireless mouse", "25.00", 5)
        val first = service.getProduct(created.id.toString())
        assertEquals("Mouse", first.name)

        repository.update(created.id, "Changed", "Changed", "30.00", 1)

        val second = service.getProduct(created.id.toString())
        assertEquals("Mouse", second.name)
    }
}
