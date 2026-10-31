package com.bittokazi.watch.navigator.security.config

import com.bittokazi.ktor.auth.services.TemplateCustomizer
import io.ktor.http.URLBuilder
import io.ktor.server.application.ApplicationCall
import io.ktor.server.sessions.sessions

class OauthTemplateCustomizerImpl(
    private val clientConfiguration: ClientConfiguration
) : TemplateCustomizer {
    override fun addExtraData(call: ApplicationCall): Map<String, Any> {
        val originalUrl = call.sessions.get("OAUTH_ORIGINAL_URL")
        if (originalUrl != null) {
            URLBuilder(originalUrl.toString()).build().parameters["client_id"]?.let { clientId ->
                clientConfiguration.clients.find { it.clientId == clientId && it.clientType == "public" }?.let { client ->
                    return mapOf(
                        "showRegister" to true,
                        "showBackButton" to false,
                    )
                }
            }
        }
        return mapOf(
            "showRegister" to false,
            "showBackButton" to true,
        )
    }
}
