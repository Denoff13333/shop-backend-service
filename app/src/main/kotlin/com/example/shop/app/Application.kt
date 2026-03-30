package com.example.shop.app

import com.example.shop.app.bootstrap.AppDependencies
import com.example.shop.app.plugins.configureHTTP
import com.example.shop.app.plugins.configureMonitoring
import com.example.shop.app.plugins.configureRouting
import com.example.shop.app.plugins.configureSecurity
import com.example.shop.app.plugins.configureSerialization
import com.example.shop.app.plugins.configureStatusPages
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.runBlocking

fun main() {
    embeddedServer(
        Netty,
        port = System.getenv("APP_PORT")?.toIntOrNull() ?: 8080,
        host = "0.0.0.0"
    ) {
        module()
    }.start(wait = true)
}

fun Application.module() {
    module(null)
}

fun Application.module(testDependencies: AppDependencies?) {
    val dependencies = testDependencies ?: AppDependencies.live(this)

    runBlocking {
        dependencies.authService.bootstrapAdmin(
            email = dependencies.config.bootstrap.adminEmail,
            password = dependencies.config.bootstrap.adminPassword
        )
    }

    configureMonitoring()
    configureSerialization()
    configureStatusPages()
    configureHTTP()
    configureSecurity(dependencies)
    configureRouting(dependencies)
}
