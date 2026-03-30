package com.example.shop.app.e2e

import com.example.shop.app.module
import com.example.shop.app.support.TestAppFactory
import com.example.shop.shared.dto.AuthResponse
import com.example.shop.shared.dto.LoginRequest
import com.example.shop.shared.dto.ProductResponse
import com.example.shop.shared.dto.RegisterRequest
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthAndProductsE2ETest {
    @Test
    fun `user can register and admin can create product visible in catalog`() = testApplication {
        val dependencies = TestAppFactory.create()

        application {
            module(testDependencies = dependencies)
        }

        val client = createClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val registerResponse = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("user@example.com", "StrongPass123!"))
        }
        assertEquals(HttpStatusCode.Created, registerResponse.status)
        val auth = registerResponse.body<AuthResponse>()
        assertTrue(auth.accessToken.isNotBlank())

        val adminLogin = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("admin@example.com", "Admin123!"))
        }
        val adminAuth = adminLogin.body<AuthResponse>()

        val createProduct = client.post("/products") {
            contentType(ContentType.Application.Json)
            bearerAuth(adminAuth.accessToken)
            setBody("""{"name":"Laptop","description":"Ultrabook","price":"999.99","stock":3}""")
        }
        assertEquals(HttpStatusCode.Created, createProduct.status)

        val listResponse = client.get("/products")
        val body = listResponse.bodyAsText()
        assertTrue(body.contains("Laptop"))
    }
}
