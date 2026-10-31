package com.bittokazi.watch.navigator.app.feedback.controllers

import com.bittokazi.watch.navigator.app.feedback.services.FeedbackService
import com.bittokazi.watch.navigator.app.feedback.services.FeedbackServiceError
import com.bittokazi.watch.navigator.app.feedback.services.FeedbackServiceError.*
import com.bittokazi.watch.navigator.config.RouteConfig.API_FEEDBACK
import com.bittokazi.watch.navigator.config.RouteConfig.API_FEEDBACK_RESOLVE
import com.bittokazi.watch.navigator.persistence.entity.CallResult
import com.bittokazi.watch.navigator.persistence.entity.PaginatedResponse
import com.bittokazi.watch.navigator.persistence.entity.ScopeEnum
import com.bittokazi.watch.navigator.security.services.scopeCheck
import com.bittokazi.watch.navigator.utils.paginatedRequestFilter
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun Application.feedbackRoute() {
    val log: Logger = LoggerFactory.getLogger(javaClass)
    val feedbackService: FeedbackService by dependencies

    routing {
        authenticate {
            post(API_FEEDBACK) {
                scopeCheck(
                    listOf(
                        ScopeEnum.FEEDBACK_CREATE,
                    ),
                    call,
                ) {
                    val payload = call.receive<FeedbackCreateRequest>()
                    when (val result = feedbackService.create(call, payload.content)) {
                        is CallResult.Success -> call.respond(HttpStatusCode.OK, result.outcome)
                        is CallResult.Failure -> handleErrorResponse(call, result)
                    }
                }
            }

            get(API_FEEDBACK) {
                scopeCheck(
                    listOf(
                        ScopeEnum.FEEDBACK_READ_ALL,
                    ),
                    call,
                ) {
                    paginatedRequestFilter(call) { paginatedRequest ->
                        val result = feedbackService.findAll(paginatedRequest)
                        val response =
                            PaginatedResponse(
                                data = result.data,
                                totalPage = result.totalPage,
                                totalCount = result.totalCount,
                                pages = result.pages,
                                prevPage = result.prevPage,
                                nextPage = result.nextPage,
                                isNextPage = result.isNextPage,
                                isPrevPage = result.isPrevPage,
                            )
                        call.respond(message = response)
                    }
                }
            }

            put(API_FEEDBACK_RESOLVE) {
                scopeCheck(
                    listOf(
                        ScopeEnum.FEEDBACK_RESOLVE,
                    ),
                    call,
                ) {
                    val id = call.parameters["id"]?.toLongOrNull() ?: -1L
                    when (val result = feedbackService.resolve(call, id)) {
                        is CallResult.Success -> call.respond(HttpStatusCode.OK, result.outcome)
                        is CallResult.Failure -> handleErrorResponse(call, result)
                    }
                }
            }
        }
    }

    log.info("[feedbackRoute] -> Routes configured.")
}

private suspend fun handleErrorResponse(
    call: ApplicationCall,
    result: CallResult.Failure<*, FeedbackServiceError>,
) {
    when (result.errorCode) {
        NOT_FOUND -> call.respond(HttpStatusCode.NotFound, result)
        UNPROCESSABLE_ENTITY -> call.respond(HttpStatusCode.UnprocessableEntity, result)
    }
}

@Serializable
data class FeedbackCreateRequest(
    val content: String,
)

