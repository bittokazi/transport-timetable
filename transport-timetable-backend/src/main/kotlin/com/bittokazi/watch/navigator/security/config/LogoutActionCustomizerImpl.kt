package com.bittokazi.watch.navigator.security.config

import com.bittokazi.ktor.auth.services.providers.OauthLogoutActionService
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect

class LogoutActionCustomizerImpl(
    val oauthConfig: OauthConfig,
) : OauthLogoutActionService {
    override suspend fun afterLogoutAction(
        userId: String?,
        clientId: String?,
        call: ApplicationCall,
    ) {
        val clientId = call.request.queryParameters["client_id"]

        if (clientId != null && oauthConfig.noRedirectClients.contains(clientId)) {
            call.respond("Logout Successful")
        } else {
            call.respondRedirect("/app/login")
        }
    }
}
