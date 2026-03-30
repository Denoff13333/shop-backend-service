@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.example.shop.app.repository

import com.example.shop.app.database.DatabaseFactory
import com.example.shop.app.database.UsersTable
import com.example.shop.app.domain.User
import com.example.shop.shared.model.UserRole
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID

class ExposedUserRepository(private val databaseFactory: DatabaseFactory) : UserRepository {
    override suspend fun create(email: String, passwordHash: String, role: UserRole): User =
        databaseFactory.dbQuery {
            val id = UUID.randomUUID()
            val createdAt = System.currentTimeMillis()

            UsersTable.insert {
                it[UsersTable.id] = id.toKotlinUuid()
                it[UsersTable.email] = email
                it[UsersTable.passwordHash] = passwordHash
                it[UsersTable.role] = role.name
                it[UsersTable.createdAt] = createdAt
            }

            User(
                id = id,
                email = email,
                passwordHash = passwordHash,
                role = role,
                createdAt = createdAt
            )
        }

    override suspend fun findByEmail(email: String): User? =
        databaseFactory.dbQuery {
            UsersTable
                .selectAll()
                .where { UsersTable.email eq email }
                .singleOrNull()
                ?.let(::mapRow)
        }

    override suspend fun findById(id: UUID): User? =
        databaseFactory.dbQuery {
            UsersTable
                .selectAll()
                .where { UsersTable.id eq id.toKotlinUuid() }
                .singleOrNull()
                ?.let(::mapRow)
        }

    override suspend fun upsertAdmin(email: String, passwordHash: String): User =
        databaseFactory.dbQuery {
            val existing = UsersTable
                .selectAll()
                .where { UsersTable.email eq email }
                .singleOrNull()

            if (existing == null) {
                val id = UUID.randomUUID()
                val createdAt = System.currentTimeMillis()
                UsersTable.insert {
                    it[UsersTable.id] = id.toKotlinUuid()
                    it[UsersTable.email] = email
                    it[UsersTable.passwordHash] = passwordHash
                    it[UsersTable.role] = UserRole.ADMIN.name
                    it[UsersTable.createdAt] = createdAt
                }
            } else {
                UsersTable.update({ UsersTable.id eq existing[UsersTable.id] }) {
                    it[UsersTable.passwordHash] = passwordHash
                    it[UsersTable.role] = UserRole.ADMIN.name
                }
            }

            UsersTable
                .selectAll()
                .where { UsersTable.email eq email }
                .single()
                .let(::mapRow)
        }

    private fun mapRow(row: ResultRow): User =
        User(
            id = row[UsersTable.id].toJavaUuid(),
            email = row[UsersTable.email],
            passwordHash = row[UsersTable.passwordHash],
            role = UserRole.valueOf(row[UsersTable.role]),
            createdAt = row[UsersTable.createdAt]
        )
}
