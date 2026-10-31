package com.bittokazi.watch.navigator.security.config

import io.ktor.server.plugins.di.annotations.Property
import kotlinx.serialization.Serializable

@Serializable
data class ClientConfiguration(
    @Property("oauth.clients") val clients: List<OAuthClientConfig>,
)

@Serializable
data class OAuthClientConfig(
    var id: String,
    var clientId: String,
    var clientName: String,
    var clientType: String,
    var redirectUris: List<String>,
    var scopes: List<String>,
    var grantTypes: List<String>,
    var clientSecret: String,
    var accessTokenValidity: Long = 300,
    var refreshTokenValidity: Long = 7200,
    var isDefault: Boolean = false,
    var consentRequired: Boolean = true,
    var postLogoutRedirectUri: String,
)
