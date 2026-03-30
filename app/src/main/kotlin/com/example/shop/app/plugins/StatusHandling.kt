package com.example.shop.app.plugins

import com.example.shop.app.exceptions.ApiException
import com.example.shop.shared.dto.ErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.respond
import org.slf4j.LoggerFactory

fun Application.configureStatusPages() {
    val logger = LoggerFactory.getLogger("StatusPages")

    install(StatusPages) {
        exception<ApiException> { call, cause ->
            logger.warn("API error: {}", cause.message)
            call.respond(
                cause.statusCode,
                ErrorResponse(
                    code = cause.code,
                    message = cause.message,
                    details = cause.details
                )
            )
        }

        exception<Throwable> { call, cause ->
            logger.error("Unhandled error", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(
                    code = "internal_error",
                    message = "Internal server error"
                )
            )
        }
    }
}
