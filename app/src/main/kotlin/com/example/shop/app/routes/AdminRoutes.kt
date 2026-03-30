package com.example.shop.app.routes

import com.example.shop.app.bootstrap.AppDependencies
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.adminRoutes(dependencies: AppDependencies) {
    authenticate("auth-jwt") {
        get("/stats/orders") {
            requireAdmin(call.currentPrincipal())
            call.respond(dependencies.adminService.getOrderStats())
        }
    }
}
