package com.bittokazi.watch.navigator.app.tenant.controllers

import com.bittokazi.watch.navigator.app.tenant.services.InfoService
import com.bittokazi.watch.navigator.app.tenant.services.InfoServiceError
import com.bittokazi.watch.navigator.config.RouteConfig.API_INFO
import com.bittokazi.watch.navigator.persistence.entity.CallResult
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun Application.infoRoute() {
    val log: Logger = LoggerFactory.getLogger(javaClass)
    val infoService: InfoService by dependencies

    routing {
        get(API_INFO) {
            when (val result = infoService.getTenantInfo(call)) {
                is CallResult.Success -> call.respond(result.outcome)

                is CallResult.Failure -> handleErrorResponse(result)
            }
        }
    }

    log.info("[infoRoute] -> Routes configured.")
}

private suspend fun RoutingContext.handleErrorResponse(callResult: CallResult.Failure<*, InfoServiceError>) {
    when (callResult.errorCode) {
        InfoServiceError.NOT_FOUND ->
            call.respond(status = HttpStatusCode.NotFound, message = callResult)

        InfoServiceError.UNPROCESSABLE_ENTITY ->
            call.respond(status = HttpStatusCode.UnprocessableEntity, message = callResult)
    }
}
