package com.bittokazi.watch.navigator.security.controllers

import com.bittokazi.watch.navigator.app.config.AppModuleConfiguration
import com.bittokazi.watch.navigator.config.RouteConfig.LOGIN_API
import com.bittokazi.watch.navigator.config.RouteConfig.OAUTH_CALLBACK_API
import com.bittokazi.watch.navigator.config.RouteConfig.REFRESH_TOKEN_API
import com.bittokazi.watch.navigator.persistence.entity.CallResult
import com.bittokazi.watch.navigator.security.entity.RefreshTokenRequest
import com.bittokazi.watch.navigator.security.services.LoginService
import com.bittokazi.watch.navigator.security.services.LoginServiceErrorCode
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun Application.configureIdpLoginRoutes() {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    val loginService: LoginService by dependencies
    val appModuleConfiguration: AppModuleConfiguration by dependencies

    routing {
        get(LOGIN_API) {
            when (val result = loginService.loginRedirect(call)) {
                is CallResult.Failure ->
                    call.respond(
                        status = HttpStatusCode.NotFound,
                        message = result,
                    )

                is CallResult.Success -> {
                    val client = result.outcome
                    call.respondRedirect(
                        "/oauth/authorize?client_id=${client.clientId}&response_type=code" +
                            "&scope=${client.scopes.joinToString("+")}" +
                            "&redirect_uri=${appModuleConfiguration.domain}/oauth/callback",
                    )
                }
            }
        }

        get(OAUTH_CALLBACK_API) {
            when (val result = loginService.fetchToken(call)) {
                is CallResult.Failure -> {
                    when (result.errorCode) {
                        LoginServiceErrorCode.CLIENT_NOT_FOUND ->
                            call.respond(
                                status = HttpStatusCode.NotFound,
                                message = result,
                            )

                        LoginServiceErrorCode.MISSING_CODE ->
                            call.respond(
                                status = HttpStatusCode.BadRequest,
                                message = result,
                            )

                        LoginServiceErrorCode.UNAUTHORIZED ->
                            call.respond(
                                status = HttpStatusCode.Unauthorized,
                                message = result,
                            )

                        LoginServiceErrorCode.BAD_REQUEST ->
                            call.respond(
                                status = HttpStatusCode.BadRequest,
                                message = result,
                            )
                    }
                }

                is CallResult.Success -> {
                    loginService.updateCookies(result.outcome, call)
                    call.respondRedirect("/app/dashboard")
                }
            }
        }

        post(REFRESH_TOKEN_API) {
            val refreshTokenRequest = call.receive<RefreshTokenRequest>()

            when (val result = loginService.fetchRefreshToken(refreshTokenRequest, call)) {
                is CallResult.Failure -> {
                    when (result.errorCode) {
                        LoginServiceErrorCode.CLIENT_NOT_FOUND ->
                            call.respond(
                                status = HttpStatusCode.NotFound,
                                message = result,
                            )

                        LoginServiceErrorCode.MISSING_CODE ->
                            call.respond(
                                status = HttpStatusCode.BadRequest,
                                message = result,
                            )

                        LoginServiceErrorCode.UNAUTHORIZED ->
                            call.respond(
                                status = HttpStatusCode.Unauthorized,
                                message = result,
                            )

                        LoginServiceErrorCode.BAD_REQUEST ->
                            call.respond(
                                status = HttpStatusCode.BadRequest,
                                message = result,
                            )
                    }
                }

                is CallResult.Success -> {
                    loginService.updateCookies(result.outcome, call)
                    call.respond(
                        status = HttpStatusCode.OK,
                        message = result,
                    )
                }
            }
        }
    }

    log.info("[configureIdpLoginRoutes] -> Routes configured.")
}
