package com.bittokazi.watch.navigator.app.user.controllers

import com.bittokazi.watch.navigator.app.user.services.UserService
import com.bittokazi.watch.navigator.app.user.services.UserServiceError
import com.bittokazi.watch.navigator.app.user.services.UserServiceError.*
import com.bittokazi.watch.navigator.config.RouteConfig.API_USER
import com.bittokazi.watch.navigator.config.RouteConfig.API_USER_BY_ID
import com.bittokazi.watch.navigator.config.RouteConfig.API_USER_REGISTER
import com.bittokazi.watch.navigator.config.RouteConfig.API_USER_UPDATE_PASSWORD
import com.bittokazi.watch.navigator.config.RouteConfig.API_USER_WHO_AM_I
import com.bittokazi.watch.navigator.config.RouteConfig.API_USER_WHO_AM_I_UPDATE_PASSWORD
import com.bittokazi.watch.navigator.persistence.entity.CallResult
import com.bittokazi.watch.navigator.persistence.entity.PaginationRequest
import com.bittokazi.watch.navigator.persistence.entity.ScopeEnum
import com.bittokazi.watch.navigator.persistence.entity.UserData
import com.bittokazi.watch.navigator.security.services.scopeCheck
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.di.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun Application.userRoute() {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    val userService: UserService by dependencies

    routing {
        authenticate {
            get(API_USER_WHO_AM_I) {
                scopeCheck(
                    listOf(
                        ScopeEnum.USER_WHO_AM_I,
                    ),
                    call,
                ) {
                    when (val callResult = userService.whoAmI(call)) {
                        is CallResult.Success ->
                            call.respond(
                                status = HttpStatusCode.OK,
                                message = callResult.outcome,
                            )

                        is CallResult.Failure -> {
                            handleErrorResponse(callResult)
                        }
                    }
                }
            }

            get(API_USER) {
                scopeCheck(
                    listOf(
                        ScopeEnum.USER_READ_ALL,
                    ),
                    call,
                ) {
                    call.respond(
                        status = HttpStatusCode.OK,
                        message =
                            userService.getAll(
                                paginationRequest =
                                    PaginationRequest(
                                        page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1,
                                        count = call.request.queryParameters["count"]?.toIntOrNull() ?: 10,
                                        query = call.request.queryParameters["query"] ?: "",
                                    ),
                            ),
                    )
                }
            }

            get(API_USER_BY_ID) {
                scopeCheck(
                    listOf(
                        ScopeEnum.USER_READ,
                    ),
                    call,
                ) {
                    when (val callResult = userService.get(call)) {
                        is CallResult.Success ->
                            call.respond(
                                status = HttpStatusCode.OK,
                                message = callResult.outcome,
                            )

                        is CallResult.Failure -> {
                            handleErrorResponse(callResult)
                        }
                    }
                }
            }

            post(API_USER) {
                scopeCheck(
                    listOf(
                        ScopeEnum.USER_ADD,
                    ),
                    call,
                ) {
                    val payload = call.receive<UserData>()

                    when (val callResult = userService.register(call, payload, false)) {
                        is CallResult.Success -> {
                            log.info("[userRoute] -> User created successfully: {}", callResult.outcome.email)
                            call.respond(
                                status = HttpStatusCode.OK,
                                message = callResult.outcome,
                            )
                        }

                        is CallResult.Failure -> {
                            log.error("[userRoute] -> User creation failed: {}, email: {}", callResult.errorCode, payload.email)
                            handleErrorResponse(callResult)
                        }
                    }
                }
            }

            put(API_USER_BY_ID) {
                scopeCheck(
                    listOf(
                        ScopeEnum.USER_UPDATE,
                    ),
                    call,
                ) {
                    val payload = call.receive<UserData>()

                    when (val callResult = userService.update(call, payload, false)) {
                        is CallResult.Success -> {
                            log.info("[userRoute] -> User updated successfully: ${callResult.outcome.email}")
                            call.respond(
                                status = HttpStatusCode.OK,
                                message = callResult.outcome,
                            )
                        }

                        is CallResult.Failure -> {
                            log.error("[userRoute] -> User update failed: ${callResult.errorCode}")
                            handleErrorResponse(callResult)
                        }
                    }
                }
            }

            put(API_USER_UPDATE_PASSWORD) {
                scopeCheck(
                    listOf(
                        ScopeEnum.USER_UPDATE,
                    ),
                    call,
                ) {
                    val payload = call.receive<UserData>()

                    when (val callResult = userService.updatePassword(call, payload, false)) {
                        is CallResult.Success -> {
                            log.info("[userRoute] -> User password updated successfully: ${callResult.outcome.email}")
                            call.respond(
                                status = HttpStatusCode.OK,
                                message = callResult.outcome,
                            )
                        }

                        is CallResult.Failure -> {
                            log.error("[userRoute] -> User password update failed: {}, user: {}", callResult.errorCode, payload.id)
                            handleErrorResponse(callResult)
                        }
                    }
                }
            }

            put(API_USER_WHO_AM_I) {
                scopeCheck(
                    listOf(
                        ScopeEnum.USER_WHO_AM_I,
                    ),
                    call,
                ) {
                    val payload = call.receive<UserData>()

                    when (val callResult = userService.update(call, payload, true)) {
                        is CallResult.Success -> {
                            log.info("[userRoute] -> Self updated successfully: {}", callResult.outcome.email)
                            call.respond(
                                status = HttpStatusCode.OK,
                                message = callResult.outcome,
                            )
                        }

                        is CallResult.Failure -> {
                            log.error("[userRoute] -> Self update failed: {}, user: {}", callResult.errorCode, payload.email)
                            handleErrorResponse(callResult)
                        }
                    }
                }
            }

            put(API_USER_WHO_AM_I_UPDATE_PASSWORD) {
                scopeCheck(
                    listOf(
                        ScopeEnum.USER_WHO_AM_I,
                    ),
                    call,
                ) {
                    val payload = call.receive<UserData>()

                    when (val callResult = userService.updatePassword(call, payload, true)) {
                        is CallResult.Success -> {
                            log.info("[userRoute] -> Self password updated successfully: {}", callResult.outcome.email)
                            call.respond(
                                status = HttpStatusCode.OK,
                                message = callResult.outcome,
                            )
                        }

                        is CallResult.Failure -> {
                            log.error("[userRoute] -> Self password update failed: {}, user: {}", callResult.errorCode, payload.id)
                            handleErrorResponse(callResult)
                        }
                    }
                }
            }
        }

        post(API_USER_REGISTER) {
            val payload = call.receive<UserData>()

            when (val callResult = userService.register(call, payload, true)) {
                is CallResult.Success -> {
                    log.info("[UserService] -> User registered: {}", payload.email)
                    call.respond(
                        status = HttpStatusCode.OK,
                        message = callResult.outcome,
                    )
                }

                is CallResult.Failure -> {
                    log.error("[UserService] -> User registered error: {}", payload.email)
                    handleErrorResponse(callResult)
                }
            }
        }
    }

    log.info("[userRoute] -> Routes configured.")
}

private suspend fun RoutingContext.handleErrorResponse(callResult: CallResult.Failure<UserData, UserServiceError>) {
    when (callResult.errorCode) {
        NOT_FOUND ->
            call.respond(
                status = HttpStatusCode.NotFound,
                message = callResult,
            )

        UNPROCESSABLE_ENTITY ->
            call.respond(
                status = HttpStatusCode.UnprocessableEntity,
                message = callResult,
            )

        EMAIL_EXIST ->
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = callResult,
            )

        PASSWORD_DOES_NOT_MATCH ->
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = callResult,
            )
    }
}
