package com.example.shop.app.routes

import com.example.shop.app.bootstrap.AppDependencies
import com.example.shop.shared.dto.LoginRequest
import com.example.shop.shared.dto.RegisterRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.authRoutes(dependencies: AppDependencies) {
    route("/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()
            val response = dependencies.authService.register(
                email = request.email,
                password = request.password
            )
            call.respond(HttpStatusCode.Created, response)
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            val response = dependencies.authService.login(
                email = request.email,
                password = request.password
            )
            call.respond(HttpStatusCode.OK, response)
        }
    }
}
