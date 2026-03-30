package com.example.shop.app.integration

import com.example.shop.app.repository.ExposedProductRepository
import com.example.shop.app.repository.ExposedUserRepository
import com.example.shop.shared.model.UserRole
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class FlywayMigrationIntegrationTest : PostgresIntegrationTestSupport() {
    @Test
    fun `migrations create usable tables`() = runBlocking {
        val userRepository = ExposedUserRepository(databaseFactory)
        val productRepository = ExposedProductRepository(databaseFactory)

        val user = userRepository.create("integration@example.com", "hash", UserRole.USER)
        val product = productRepository.create("Keyboard", "Mechanical", "100.00", 10)

        assertNotNull(userRepository.findById(user.id))
        assertEquals("Keyboard", productRepository.findById(product.id)?.name)
    }
}
