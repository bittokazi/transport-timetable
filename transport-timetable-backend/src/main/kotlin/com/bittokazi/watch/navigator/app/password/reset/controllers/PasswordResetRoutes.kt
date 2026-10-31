package com.bittokazi.watch.navigator.app.password.reset.controllers

import com.bittokazi.watch.navigator.app.password.reset.services.PasswordResetRequest
import com.bittokazi.watch.navigator.app.password.reset.services.PasswordResetService
import com.bittokazi.watch.navigator.app.password.reset.services.PasswordResetTokenError
import com.bittokazi.watch.navigator.app.password.reset.services.PasswordResetTokenError.*
import com.bittokazi.watch.navigator.app.password.reset.services.PasswordResetTokenRequest
import com.bittokazi.watch.navigator.config.RouteConfig.API
import com.bittokazi.watch.navigator.config.RouteConfig.V1
import com.bittokazi.watch.navigator.persistence.entity.CallResult
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import org.slf4j.Logger
import org.slf4j.LoggerFactory

const val API_PASSWORD_RESET_TOKEN_REQUEST = "$API/$V1/users/password-reset/request-token"
const val API_PASSWORD_RESET_TOKEN_VERIFY = "$API/$V1/users/password-reset/{token}"
const val API_PASSWORD_RESET = "$API/$V1/users/password-reset/{token}"

fun Application.passwordResetRoutes() {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    val passwordResetService: PasswordResetService by dependencies

    routing {
        post(API_PASSWORD_RESET_TOKEN_REQUEST) {
            val passwordResetTokenRequest = call.receive<PasswordResetTokenRequest>()

            when (val result = passwordResetService.create(passwordResetTokenRequest)) {
                is CallResult.Success ->
                    call.respond(
                        mapOf("message" to "If an account with that email exists, a password reset token has been sent."),
                    )

                is CallResult.Failure -> handleErrorResponse(result)
            }
        }

        get(API_PASSWORD_RESET_TOKEN_VERIFY) {
            val token =
                call.parameters["token"] ?: return@get call.respond(
                    status = HttpStatusCode.BadRequest,
                    message =
                        mapOf(
                            "error" to "missing_token",
                            "message" to "Password reset token is required.",
                        ),
                )

            when (val result = passwordResetService.find(token)) {
                is CallResult.Success ->
                    call.respond(
                        mapOf("message" to "Password reset token is valid."),
                    )

                is CallResult.Failure -> handleErrorResponse(result)
            }
        }

        post(API_PASSWORD_RESET) {
            val token =
                call.parameters["token"] ?: return@post call.respond(
                    status = HttpStatusCode.BadRequest,
                    message =
                        mapOf(
                            "error" to "missing_token",
                            "message" to "Password reset token is required.",
                        ),
                )

            val passwordResetRequest = call.receive<PasswordResetRequest>()

            when (val result = passwordResetService.reset(token, passwordResetRequest)) {
                is CallResult.Success ->
                    call.respond(
                        mapOf("message" to "Password reset successful."),
                    )

                is CallResult.Failure -> handleErrorResponse(result)
            }
        }
    }

    log.info("[passwordResetRoutes] -> Routes configured.")
}

private suspend fun RoutingContext.handleErrorResponse(result: CallResult.Failure<*, PasswordResetTokenError>) {
    when (result.errorCode) {
        USER_NOT_FOUND ->
            call.respond(
                status = HttpStatusCode.NotFound,
                message =
                    mapOf(
                        "error" to result.errorCode,
                        "message" to "User not found.",
                    ),
            )
        TOKEN_NOT_FOUND ->
            call.respond(
                status = HttpStatusCode.NotFound,
                message =
                    mapOf(
                        "error" to result.errorCode,
                        "message" to "Password reset token not found.",
                    ),
            )
        TOKEN_EXPIRED ->
            call.respond(
                status = HttpStatusCode.BadRequest,
                message =
                    mapOf(
                        "error" to result.errorCode,
                        "message" to "Password reset token has expired.",
                    ),
            )
        PASSWORD_MISMATCH ->
            call.respond(
                status = HttpStatusCode.BadRequest,
                message =
                    mapOf(
                        "error" to result.errorCode,
                        "message" to "Passwords do not match.",
                    ),
            )
        INTERNAL_ERROR ->
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message =
                    mapOf(
                        "error" to result.errorCode,
                        "message" to "An internal error occurred while processing the password reset request.",
                    ),
            )
    }
}
