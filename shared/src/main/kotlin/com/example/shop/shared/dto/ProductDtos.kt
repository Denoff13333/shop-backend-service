package com.example.shop.shared.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductRequest(
    val name: String,
    val description: String,
    val price: String,
    val stock: Int
)

@Serializable
data class ProductResponse(
    val id: String,
    val name: String,
    val description: String,
    val price: String,
    val stock: Int,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class ProductListResponse(
    val items: List<ProductResponse>
)
