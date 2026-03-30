package com.example.shop.app.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.example.shop.app.config.JwtConfig
import com.example.shop.app.domain.User
import java.util.Date

class JwtTokenProvider(private val config: JwtConfig) {
    private val algorithm = Algorithm.HMAC256(config.secret)

    fun generateToken(user: User): String {
        val now = System.currentTimeMillis()
        val expiresAt = now + (config.expiresInSeconds * 1000)

        return JWT.create()
            .withAudience(config.audience)
            .withIssuer(config.issuer)
            .withSubject(user.id.toString())
            .withClaim("email", user.email)
            .withClaim("role", user.role.name)
            .withIssuedAt(Date(now))
            .withExpiresAt(Date(expiresAt))
            .sign(algorithm)
    }

    fun verifier(): JWTVerifier =
        JWT.require(algorithm)
            .withAudience(config.audience)
            .withIssuer(config.issuer)
            .build()
}
