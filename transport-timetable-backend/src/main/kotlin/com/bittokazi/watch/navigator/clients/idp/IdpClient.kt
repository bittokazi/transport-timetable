package com.bittokazi.watch.navigator.clients.idp

import com.bittokazi.ktor.auth.domains.token.OauthTokenResponse
import com.bittokazi.ktor.auth.services.providers.OAuthClientDTO
import com.bittokazi.watch.navigator.app.config.AppModuleConfiguration
import com.bittokazi.watch.navigator.clients.common.createClientLogging
import com.bittokazi.watch.navigator.persistence.entity.CallResult
import com.bittokazi.watch.navigator.security.config.OAuthClientConfig
import com.bittokazi.watch.navigator.security.entity.RefreshTokenRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.plugins.di.annotations.Property
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class IdpClient(
    val idpClientConfig: IdpClientConfig,
    val appModuleConfiguration: AppModuleConfiguration
) {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    private val applicationHttpClient: HttpClient =
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    },
                )
            }
            // Custom interceptor plugin for request/response logging
            install(createClientLogging("Custom${javaClass.name}Logging", log))
        }

    init {
        log.info("[IdpClient] -> created")
    }

    suspend fun fetchAccessToken(
        code: String,
        client: OAuthClientDTO,
        oauthClientConfig: OAuthClientConfig
    ): CallResult<OauthTokenResponse, IdpClientErrorCode> {
        val response =
            applicationHttpClient.submitForm(
                url = "${idpClientConfig.endpoint}/oauth/token",
                formParameters =
                    Parameters.build {
                        append("grant_type", "authorization_code")
                        append("code", code)
                        append("redirect_uri", "${appModuleConfiguration.domain}/oauth/callback")
                        append("client_id", client.clientId)
                        append("client_secret", oauthClientConfig.clientSecret)
                    },
            )

        return when (response.status) {
            HttpStatusCode.OK -> CallResult.Success(response.body<OauthTokenResponse>())
            HttpStatusCode.Unauthorized -> CallResult.Failure(IdpClientErrorCode.UNAUTHORIZED)
            else -> CallResult.Failure(IdpClientErrorCode.BAD_REQUEST)
        }
    }

    suspend fun fetchRefreshToken(
        refreshTokenRequest: RefreshTokenRequest,
        client: OAuthClientDTO,
        oauthClientConfig: OAuthClientConfig
    ): CallResult<OauthTokenResponse, IdpClientErrorCode> {
        val response: HttpResponse =
            applicationHttpClient.submitForm(
                url = "${idpClientConfig.endpoint}/oauth/token",
                formParameters =
                    Parameters.build {
                        append("grant_type", "refresh_token")
                        append("refresh_token", refreshTokenRequest.refreshToken)
                        append("client_id", client.clientId)
                        append("client_secret", oauthClientConfig.clientSecret)
                    },
            )

        return when (response.status) {
            HttpStatusCode.OK -> CallResult.Success(response.body<OauthTokenResponse>())
            HttpStatusCode.Unauthorized -> CallResult.Failure(IdpClientErrorCode.UNAUTHORIZED)
            else -> CallResult.Failure(IdpClientErrorCode.BAD_REQUEST)
        }
    }
}

data class IdpClientConfig(
    @Property("idp-client-config.enabled") val enabled: Boolean,
    @Property("idp-client-config.end-point") val endpoint: String,
)

enum class IdpClientErrorCode {
    UNAUTHORIZED,
    BAD_REQUEST,
}
