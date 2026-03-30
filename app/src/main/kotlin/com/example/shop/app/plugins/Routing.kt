package com.example.shop.app.plugins

import com.example.shop.app.bootstrap.AppDependencies
import com.example.shop.app.routes.adminRoutes
import com.example.shop.app.routes.authRoutes
import com.example.shop.app.routes.healthRoutes
import com.example.shop.app.routes.orderRoutes
import com.example.shop.app.routes.productRoutes
import io.ktor.server.application.Application
import io.ktor.server.plugins.openapi.openAPI
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.routing

fun Application.configureRouting(dependencies: AppDependencies) {
    routing {
        healthRoutes()
        authRoutes(dependencies)
        productRoutes(dependencies)
        orderRoutes(dependencies)
        adminRoutes(dependencies)

        openAPI(path = "openapi", swaggerFile = "openapi/documentation.yaml")
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
    }
}
