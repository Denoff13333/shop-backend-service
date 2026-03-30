package com.example.shop.app.security

import org.mindrot.jbcrypt.BCrypt

interface PasswordHasher {
    fun hash(rawPassword: String): String
    fun verify(rawPassword: String, hashedPassword: String): Boolean
}

class BCryptPasswordHasher : PasswordHasher {
    override fun hash(rawPassword: String): String = BCrypt.hashpw(rawPassword, BCrypt.gensalt())

    override fun verify(rawPassword: String, hashedPassword: String): Boolean =
        BCrypt.checkpw(rawPassword, hashedPassword)
}
