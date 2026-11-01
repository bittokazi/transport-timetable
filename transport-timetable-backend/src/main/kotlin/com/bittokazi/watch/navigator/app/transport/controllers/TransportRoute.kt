package com.bittokazi.watch.navigator.app.transport.controllers

import com.bittokazi.watch.navigator.app.transport.services.TransportService
import com.bittokazi.watch.navigator.app.transport.services.TransportServiceError
import com.bittokazi.watch.navigator.config.RouteConfig.API
import com.bittokazi.watch.navigator.config.RouteConfig.V1
import com.bittokazi.watch.navigator.persistence.entity.CallResult
import com.bittokazi.watch.navigator.persistence.entity.ScopeEnum
import com.bittokazi.watch.navigator.security.services.scopeCheck
import com.bittokazi.watch.navigator.validation.config.ValidationConfiguration
import com.bittokazi.watch.navigator.validation.services.ValidationService
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.slf4j.Logger
import org.slf4j.LoggerFactory

const val TRANSPORT_API = "$API/$V1/transport"
const val TRANSPORT_SEARCH = "$TRANSPORT_API/search"
const val TRANSPORT_PLAN = "$TRANSPORT_API/plan"
const val TRANSPORT_MAP = "$TRANSPORT_API/map"

fun Application.transportRoute() {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    val transportService: TransportService by dependencies

    val validationService: ValidationService by dependencies

    val validationConfiguration: ValidationConfiguration by dependencies

    routing {
        authenticate {
            // Search endpoint - GET /api/v1/transport/search?query=...
            get(TRANSPORT_SEARCH) {
                scopeCheck(
                    listOf(
                        ScopeEnum.USER_TRANSPORT_SEARCH,
                    ),
                    call,
                ) {
                    val query = call.request.queryParameters["query"]

                    when (query.isNullOrEmpty()) {
                        true ->
                            call.respond(
                                status = HttpStatusCode.BadRequest,
                                message = mapOf("error" to "missing query"),
                            )

                        else ->
                            when (val result = transportService.search(query)) {
                                is CallResult.Success ->
                                    call.respond(
                                        status = HttpStatusCode.OK,
                                        message = result.outcome,
                                    )

                                is CallResult.Failure -> handleErrorResponse(result)
                            }
                    }
                }
            }

            // Plan endpoint - GET /api/v1/transport/plan?from=...&to=...&time=...&arriveBy=...
            get(TRANSPORT_PLAN) {
                scopeCheck(
                    listOf(
                        ScopeEnum.USER_TRANSPORT_PLAN,
                    ),
                    call,
                ) {
                    when (val principal = call.principal<JWTPrincipal>()) {
                        is JWTPrincipal ->
                            handlePlanRequest(
                                validationService,
                                principal,
                                transportService,
                                validationConfiguration,
                            )

                        else ->
                            handleErrorResponse(
                                CallResult.Failure<Any, TransportServiceError>(TransportServiceError.PLAN_FAILED),
                            )
                    }
                }
            }

            // Map endpoint - GET /api/v1/transport/map?...
            get(TRANSPORT_MAP) {
                scopeCheck(
                    listOf(
                        ScopeEnum.USER_TRANSPORT_MAP,
                    ),
                    call,
                ) {
                    // Preserve repeated query params such as style, markers, and path.
                    val queryString = call.request.queryParameters.entries()
                        .flatMap { (key, values) ->
                            values.map { value -> "$key=$value" }
                        }
                        .joinToString("&")

                    when (val result = transportService.getStaticMap(queryString)) {
                        is CallResult.Success -> {
                            call.respondBytes(
                                bytes = result.outcome,
                                contentType = ContentType.Image.PNG,
                                status = HttpStatusCode.OK,
                            )
                        }

                        is CallResult.Failure -> handleErrorResponse(result)
                    }
                }
            }
        }
    }

    log.info("[transportRoute] -> Routes configured.")
}

private suspend fun RoutingContext.handlePlanRequest(
    validationService: ValidationService,
    principal: JWTPrincipal,
    transportService: TransportService,
    validationConfiguration: ValidationConfiguration,
) {
    validationService.isValid(principal.subject?.toLongOrNull() ?: -1)
        .let { isValid ->
            when (isValid) {
                is CallResult.Success -> {
                    when (isValid.outcome) {
                        true -> {
                            handleValidPlanRequest(transportService, validationService, principal)
                        }

                        false ->
                            call.respond(
                                status = HttpStatusCode.UnprocessableEntity,
                                message =
                                    mapOf(
                                        "error" to "limit_reached",
                                        "message" to "You have reached limit ${validationConfiguration.limit} " +
                                            "monthly trip planner calls.",
                                    ),
                            )
                    }
                }

                is CallResult.Failure ->
                    handleErrorResponse(
                        CallResult.Failure<Any, TransportServiceError>(TransportServiceError.PLAN_FAILED),
                    )
            }
        }
}

private suspend fun RoutingContext.handleValidPlanRequest(
    transportService: TransportService,
    validationService: ValidationService,
    principal: JWTPrincipal,
) {
    val from = call.request.queryParameters["from"]
    val to = call.request.queryParameters["to"]
    val time = call.request.queryParameters["time"]
    val arriveBy = call.request.queryParameters["arriveBy"]

    when (from.isNullOrEmpty() || to.isNullOrEmpty()) {
        true ->
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = mapOf("error" to "missing from/to"),
            )

        else ->
            when (val result = transportService.plan(from, to, time, arriveBy)) {
                is CallResult.Success -> {
                    validationService.countUp(
                        principal.subject?.toLongOrNull() ?: -1,
                    )

                    call.respond(
                        status = HttpStatusCode.OK,
                        message = result.outcome,
                    )
                }

                is CallResult.Failure -> handleErrorResponse(result)
            }
    }
}

private suspend fun RoutingContext.handleErrorResponse(callResult: CallResult.Failure<*, TransportServiceError>) {
    when (callResult.errorCode) {
        TransportServiceError.SEARCH_FAILED ->
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = callResult,
            )

        TransportServiceError.PLAN_FAILED ->
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = callResult,
            )

        TransportServiceError.MAP_FAILED ->
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = callResult,
            )
    }
}
