package com.bittokazi.watch.navigator.security

import at.favre.lib.crypto.bcrypt.BCrypt
import com.bittokazi.ktor.auth.configureOauth2AuthorizationServer
import com.bittokazi.ktor.auth.database.OauthDatabaseConfiguration
import com.bittokazi.ktor.auth.oauthAuthenticationConfig
import com.bittokazi.ktor.auth.services.*
import com.bittokazi.ktor.auth.services.providers.*
import com.bittokazi.ktor.auth.services.providers.database.*
import com.bittokazi.ktor.auth.services.providers.inmemory.OauthClientServiceInMemoryProvider
import com.bittokazi.watch.navigator.security.config.ClientConfiguration
import com.bittokazi.watch.navigator.security.config.JwtCustomizerImpl
import com.bittokazi.watch.navigator.security.config.LogoutActionCustomizerImpl
import com.bittokazi.watch.navigator.security.config.OauthConfig
import com.bittokazi.watch.navigator.security.config.OauthTemplateCustomizerImpl
import com.bittokazi.watch.navigator.security.config.SessionExtenderImpl
import com.bittokazi.watch.navigator.security.database.OauthDatabaseConfigurationImpl
import com.bittokazi.watch.navigator.security.services.LoginService
import com.bittokazi.watch.navigator.security.services.OauthUserServiceImpl
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.di.*
import io.ktor.server.plugins.requestvalidation.*
import java.util.UUID

fun Application.configureSecurityModule() {
    dependencies {
        provide(ClientConfiguration::class)
        provide<OauthConfig>(OauthConfig::class)
    }

    val clientConfiguration: ClientConfiguration by dependencies
    val oauthConfig: OauthConfig by dependencies

    if (oauthConfig.enabled) {
        dependencies {
            provide<OauthDatabaseConfiguration>(OauthDatabaseConfigurationImpl::class)
            provide<OauthClientService> {
                OauthClientServiceInMemoryProvider(
                    clientConfiguration.clients.map { client ->
                        OAuthClientDTO(
                            id = UUID.fromString(client.id),
                            consentRequired = client.consentRequired,
                            clientId = client.clientId,
                            clientSecret = BCrypt.withDefaults().hashToString(12, client.clientSecret.toCharArray()),
                            clientName = client.clientName,
                            clientType = client.clientType,
                            scopes = client.scopes,
                            redirectUris = client.redirectUris,
                            grantTypes = client.grantTypes,
                            accessTokenValidity = client.accessTokenValidity,
                            refreshTokenValidity = client.refreshTokenValidity,
                        )
                    }.toMutableList(),
                )
            }
            provide<OauthUserService>(OauthUserServiceImpl::class)
        }

        authentication {
            oauthAuthenticationConfig(oauthConfig.issuer)
        }
        dependencies {
            provide<OauthAuthorizationCodeService>(OauthAuthorizationCodeServiceDatabaseProvider::class)
            provide<OauthTokenService>(OauthTokenServiceDatabaseProvider::class)
            provide<OauthConsentService>(OauthConsentServiceDatabaseProvider::class)
            provide<OauthDeviceCodeService>(OauthDeviceCodeServiceDatabaseProvider::class)

            provide<JwtTokenCustomizer>(JwtCustomizerImpl::class)
            provide(JwtVerifier::class)
            provide(SessionCustomizer::class)
            provide<TemplateCustomizer>(OauthTemplateCustomizerImpl::class)
            provide<TemplateCustomizerFactory>(DefaultTemplateCustomizerFactory::class)
            provide<OauthLoginOptionService<AuthenticatedUserSession>> {
                DefaultOauthLoginOptionService("/app/dashboard")
            }
            provide<OauthLogoutActionService>(LogoutActionCustomizerImpl::class)

            provide<SessionExtender>(SessionExtenderImpl::class)

            provide(LoginService::class)
        }

        configureOauth2AuthorizationServer(
            configureSerialization = false,
            defaultLoginRoutes = true,
            defaultAuthorizeRoute = true,
            defaultOidcRoute = true,
            defaultTokenRoute = true,
            defaultConsentRoute = true,
            configureForwardHeaderAndDefaultHeadersPlugin = false,
        )
    }

    install(RequestValidation)
}
