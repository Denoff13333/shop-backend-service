package com.example.shop.app.routes

import com.example.shop.app.bootstrap.AppDependencies
import com.example.shop.app.exceptions.ValidationException
import com.example.shop.shared.dto.CreateOrderRequest
import com.example.shop.shared.dto.OrderListResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import java.util.UUID

fun Route.orderRoutes(dependencies: AppDependencies) {
    authenticate("auth-jwt") {
        post("/orders") {
            val principal = call.currentPrincipal()
            val request = call.receive<CreateOrderRequest>()
            val response = dependencies.orderService.createOrder(principal.userId, request)
            call.respond(HttpStatusCode.Created, response)
        }

        get("/orders") {
            val principal = call.currentPrincipal()
            val response = dependencies.orderService.listOrders(principal.userId)
            call.respond(OrderListResponse(response))
        }

        delete("/orders/{id}") {
            val principal = call.currentPrincipal()
            val rawId = requirePathParameter(call.parameters["id"], "id")
            val orderId = runCatching { UUID.fromString(rawId) }
                .getOrElse { throw ValidationException("Invalid order id: $rawId") }

            val response = dependencies.orderService.cancelOrder(principal.userId, orderId)
            call.respond(HttpStatusCode.OK, response)
        }
    }
}
