package com.example.shop.app.routes

import com.example.shop.app.exceptions.ForbiddenException
import com.example.shop.app.exceptions.ValidationException
import com.example.shop.app.security.AppPrincipal
import com.example.shop.shared.model.UserRole
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.principal

fun ApplicationCall.currentPrincipal(): AppPrincipal =
    principal<AppPrincipal>() ?: throw ForbiddenException("Missing principal")

fun requireAdmin(principal: AppPrincipal) {
    if (principal.role != UserRole.ADMIN) {
        throw ForbiddenException("Admin access required")
    }
}

fun requirePathParameter(value: String?, name: String): String =
    value ?: throw ValidationException("Path parameter '$name' is required")
