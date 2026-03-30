package com.example.shop.app.e2e

import com.example.shop.app.module
import com.example.shop.app.support.TestAppFactory
import com.example.shop.shared.dto.AuthResponse
import com.example.shop.shared.dto.CreateOrderItemRequest
import com.example.shop.shared.dto.CreateOrderRequest
import com.example.shop.shared.dto.LoginRequest
import com.example.shop.shared.dto.OrderResponse
import com.example.shop.shared.dto.RegisterRequest
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.get
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OrderLifecycleE2ETest {
    @Test
    fun `user can create and cancel own order`() = testApplication {
        val dependencies = TestAppFactory.create()

        application {
            module(testDependencies = dependencies)
        }

        val client = createClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val adminAuth = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("admin@example.com", "Admin123!"))
        }.body<AuthResponse>()

        val createProductResponse = client.post("/products") {
            contentType(ContentType.Application.Json)
            bearerAuth(adminAuth.accessToken)
            setBody("""{"name":"SSD","description":"1TB NVMe","price":"120.00","stock":10}""")
        }
        val product = createProductResponse.body<com.example.shop.shared.dto.ProductResponse>()

        val userAuth = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("buyer@example.com", "StrongPass123!"))
        }.body<AuthResponse>()

        val createOrder = client.post("/orders") {
            contentType(ContentType.Application.Json)
            bearerAuth(userAuth.accessToken)
            setBody(
                CreateOrderRequest(
                    items = listOf(CreateOrderItemRequest(product.id, 2))
                )
            )
        }
        assertEquals(HttpStatusCode.Created, createOrder.status)
        val order = createOrder.body<OrderResponse>()

        val cancelOrder = client.delete("/orders/${order.id}") {
            bearerAuth(userAuth.accessToken)
        }
        assertEquals(HttpStatusCode.OK, cancelOrder.status)
        assertTrue(cancelOrder.bodyAsText().contains("CANCELLED"))

        val history = client.get("/orders") {
            bearerAuth(userAuth.accessToken)
        }
        assertTrue(history.bodyAsText().contains(order.id))
    }
}
