package com.example.shop.app.security

import com.example.shop.shared.model.UserRole
import io.ktor.server.auth.Principal
import java.util.UUID

data class AppPrincipal(
    val userId: UUID,
    val email: String,
    val role: UserRole
) : Principal
