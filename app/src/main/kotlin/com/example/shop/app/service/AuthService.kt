package com.example.shop.app.service

import com.example.shop.app.exceptions.ConflictException
import com.example.shop.app.exceptions.UnauthorizedException
import com.example.shop.app.exceptions.ValidationException
import com.example.shop.app.repository.UserRepository
import com.example.shop.app.security.JwtTokenProvider
import com.example.shop.app.security.PasswordHasher
import com.example.shop.app.util.toAuthResponse
import com.example.shop.shared.dto.AuthResponse
import com.example.shop.shared.model.UserRole

class AuthService(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
    private val tokenProvider: JwtTokenProvider
) {
    suspend fun register(email: String, password: String): AuthResponse {
        validateCredentials(email, password)
        val normalizedEmail = email.lowercase()

        if (userRepository.findByEmail(normalizedEmail) != null) {
            throw ConflictException("User with this email already exists")
        }

        val user = userRepository.create(
            email = normalizedEmail,
            passwordHash = passwordHasher.hash(password),
            role = UserRole.USER
        )

        return user.toAuthResponse(tokenProvider.generateToken(user))
    }

    suspend fun login(email: String, password: String): AuthResponse {
        validateCredentials(email, password)

        val user = userRepository.findByEmail(email.lowercase())
            ?: throw UnauthorizedException("Invalid email or password")

        if (!passwordHasher.verify(password, user.passwordHash)) {
            throw UnauthorizedException("Invalid email or password")
        }

        return user.toAuthResponse(tokenProvider.generateToken(user))
    }

    suspend fun bootstrapAdmin(email: String, password: String) {
        validateCredentials(email, password)
        userRepository.upsertAdmin(email.lowercase(), passwordHasher.hash(password))
    }

    private fun validateCredentials(email: String, password: String) {
        if (!email.contains("@")) {
            throw ValidationException("Email is invalid")
        }
        if (password.length < 8) {
            throw ValidationException("Password must contain at least 8 characters")
        }
    }
}
