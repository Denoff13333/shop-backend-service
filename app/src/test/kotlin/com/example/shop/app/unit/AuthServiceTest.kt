package com.example.shop.app.unit

import com.example.shop.app.security.BCryptPasswordHasher
import com.example.shop.app.security.JwtTokenProvider
import com.example.shop.app.service.AuthService
import com.example.shop.app.support.InMemoryUserRepository
import com.example.shop.app.config.JwtConfig
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class AuthServiceTest {
    @Test
    fun `register hashes password and returns token`() = runTest {
        val repository = InMemoryUserRepository()
        val service = AuthService(
            userRepository = repository,
            passwordHasher = BCryptPasswordHasher(),
            tokenProvider = JwtTokenProvider(
                JwtConfig(
                    secret = "test-secret",
                    issuer = "issuer",
                    audience = "audience",
                    realm = "realm",
                    expiresInSeconds = 3600
                )
            )
        )

        val response = service.register("user@example.com", "StrongPass123!")

        val savedUser = repository.findByEmail("user@example.com")
        assertEquals("user@example.com", response.user.email)
        assertTrue(response.accessToken.isNotBlank())
        assertTrue(savedUser != null)
        assertNotEquals("StrongPass123!", savedUser!!.passwordHash)
    }
}
