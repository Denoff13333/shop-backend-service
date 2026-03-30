package com.example.shop.app.routes

import com.example.shop.app.bootstrap.AppDependencies
import com.example.shop.shared.dto.ProductListResponse
import com.example.shop.shared.dto.ProductRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put

fun Route.productRoutes(dependencies: AppDependencies) {
    get("/products") {
        val products = dependencies.productService.listProducts()
        call.respond(ProductListResponse(products))
    }

    get("/products/{id}") {
        val id = requirePathParameter(call.parameters["id"], "id")
        val product = dependencies.productService.getProduct(id)
        call.respond(product)
    }

    authenticate("auth-jwt") {
        post("/products") {
            requireAdmin(call.currentPrincipal())
            val request = call.receive<ProductRequest>()
            val product = dependencies.productService.createProduct(
                name = request.name,
                description = request.description,
                price = request.price,
                stock = request.stock
            )
            call.respond(HttpStatusCode.Created, product)
        }

        put("/products/{id}") {
            requireAdmin(call.currentPrincipal())
            val id = requirePathParameter(call.parameters["id"], "id")
            val request = call.receive<ProductRequest>()
            val product = dependencies.productService.updateProduct(
                id = id,
                name = request.name,
                description = request.description,
                price = request.price,
                stock = request.stock
            )
            call.respond(HttpStatusCode.OK, product)
        }

        delete("/products/{id}") {
            requireAdmin(call.currentPrincipal())
            val id = requirePathParameter(call.parameters["id"], "id")
            dependencies.productService.deleteProduct(id)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
