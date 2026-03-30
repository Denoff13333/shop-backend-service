package com.example.shop.shared.dto

import com.example.shop.shared.model.UserRole
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class UserInfoResponse(
    val id: String,
    val email: String,
    val role: UserRole
)

@Serializable
data class AuthResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val user: UserInfoResponse
)
