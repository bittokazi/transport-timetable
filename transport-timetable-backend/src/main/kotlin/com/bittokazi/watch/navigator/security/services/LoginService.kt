package com.bittokazi.watch.navigator.security.services

import com.bittokazi.ktor.auth.domains.token.OauthTokenResponse
import com.bittokazi.ktor.auth.services.providers.OAuthClientDTO
import com.bittokazi.ktor.auth.services.providers.OauthClientService
import com.bittokazi.watch.navigator.clients.idp.IdpClient
import com.bittokazi.watch.navigator.clients.idp.IdpClientErrorCode
import com.bittokazi.watch.navigator.persistence.entity.CallResult
import com.bittokazi.watch.navigator.security.config.ClientConfiguration
import com.bittokazi.watch.navigator.security.entity.RefreshTokenRequest
import io.ktor.http.Cookie
import io.ktor.server.application.ApplicationCall
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LoginService(
    private val oauthClientService: OauthClientService,
    private val idpClient: IdpClient,
    private val oauthClientConfiguration: ClientConfiguration
) {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    init {
        log.info("[LoginService] -> init")
    }

    companion object {
        private const val CODE = "code"
    }

    fun loginRedirect(call: ApplicationCall): CallResult<OAuthClientDTO, LoginServiceErrorCode> {
        return oauthClientService.findDefaultClient(call)?.let {
            CallResult.Success(it)
        } ?: CallResult.Failure(LoginServiceErrorCode.CLIENT_NOT_FOUND)
    }

    suspend fun fetchToken(call: ApplicationCall): CallResult<OauthTokenResponse, LoginServiceErrorCode> {
        val code =
            call.parameters[CODE]
                ?: return CallResult.Failure(LoginServiceErrorCode.MISSING_CODE)

        val client =
            oauthClientService.findDefaultClient(call)
                ?: return CallResult.Failure(LoginServiceErrorCode.CLIENT_NOT_FOUND)

        val result =
            idpClient.fetchAccessToken(
                code = code,
                client = client,
                oauthClientConfig = oauthClientConfiguration.clients.find { it.clientId == client.clientId }!!
            )

        return when (result) {
            is CallResult.Success -> CallResult.Success(result.outcome)
            is CallResult.Failure -> {
                when (result.errorCode) {
                    IdpClientErrorCode.UNAUTHORIZED -> CallResult.Failure(LoginServiceErrorCode.UNAUTHORIZED)
                    IdpClientErrorCode.BAD_REQUEST -> CallResult.Failure(LoginServiceErrorCode.BAD_REQUEST)
                }
            }
        }
    }

    suspend fun fetchRefreshToken(
        refreshTokenRequest: RefreshTokenRequest,
        call: ApplicationCall,
    ): CallResult<OauthTokenResponse, LoginServiceErrorCode> {
        val client =
            oauthClientService.findDefaultClient(call)
                ?: return CallResult.Failure(LoginServiceErrorCode.CLIENT_NOT_FOUND)

        val result =
            idpClient.fetchRefreshToken(
                refreshTokenRequest = refreshTokenRequest,
                client = client,
                oauthClientConfig = oauthClientConfiguration.clients.find { it.clientId == client.clientId }!!
            )

        return when (result) {
            is CallResult.Success -> CallResult.Success(result.outcome)
            is CallResult.Failure -> {
                when (result.errorCode) {
                    IdpClientErrorCode.UNAUTHORIZED -> CallResult.Failure(LoginServiceErrorCode.UNAUTHORIZED)
                    IdpClientErrorCode.BAD_REQUEST -> CallResult.Failure(LoginServiceErrorCode.BAD_REQUEST)
                }
            }
        }
    }

    fun updateCookies(
        tokenResponse: OauthTokenResponse,
        call: ApplicationCall,
    ) {
        tokenResponse.access_token?.let {
            call.response.cookies.append(
                Cookie("access_token", it, path = "/", maxAge = 31536001),
            )
        }
        tokenResponse.refresh_token?.let {
            call.response.cookies.append(
                Cookie("refresh_token", it, path = "/", maxAge = 31536001),
            )
        }
    }
}

enum class LoginServiceErrorCode {
    CLIENT_NOT_FOUND,
    MISSING_CODE,
    UNAUTHORIZED,
    BAD_REQUEST,
}
