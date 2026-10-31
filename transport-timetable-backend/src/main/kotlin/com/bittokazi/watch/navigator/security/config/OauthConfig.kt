package com.bittokazi.watch.navigator.security.config

import io.ktor.server.plugins.di.annotations.Property

data class OauthConfig(
    @Property("oauth.enabled") val enabled: Boolean,
    @Property("oauth.issuer") val issuer: String,
    @Property("oauth.no-redirect-clients") val noRedirectClients: List<String> = listOf(),
)
