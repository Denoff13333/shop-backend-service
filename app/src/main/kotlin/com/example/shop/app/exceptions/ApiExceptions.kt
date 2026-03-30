package com.example.shop.app.exceptions

import io.ktor.http.HttpStatusCode

open class ApiException(
    val statusCode: HttpStatusCode,
    val code: String,
    override val message: String,
    val details: String? = null
) : RuntimeException(message)

class ValidationException(message: String, details: String? = null) :
    ApiException(HttpStatusCode.BadRequest, "validation_error", message, details)

class UnauthorizedException(message: String = "Unauthorized") :
    ApiException(HttpStatusCode.Unauthorized, "unauthorized", message)

class ForbiddenException(message: String = "Access denied") :
    ApiException(HttpStatusCode.Forbidden, "forbidden", message)

class NotFoundException(message: String) :
    ApiException(HttpStatusCode.NotFound, "not_found", message)

class ConflictException(message: String) :
    ApiException(HttpStatusCode.Conflict, "conflict", message)

class BusinessRuleException(message: String) :
    ApiException(HttpStatusCode.BadRequest, "business_error", message)
