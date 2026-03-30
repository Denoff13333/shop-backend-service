package com.example.shop.app.plugins

import com.example.shop.app.bootstrap.AppDependencies
import com.example.shop.app.security.AppPrincipal
import com.example.shop.shared.model.UserRole
import io.ktor.server.application.*
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.*
import java.util.UUID

fun Application.configureSecurity(dependencies: AppDependencies) {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = dependencies.config.jwt.realm
            verifier(dependencies.tokenProvider.verifier())
            validate { credential ->
                val subject = credential.payload.subject ?: return@validate null
                val email = credential.payload.getClaim("email").asString() ?: return@validate null
                val role = credential.payload.getClaim("role").asString() ?: return@validate null

                AppPrincipal(
                    userId = UUID.fromString(subject),
                    email = email,
                    role = UserRole.valueOf(role)
                )
            }
        }
    }
}
