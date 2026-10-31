package com.bittokazi.watch.navigator.clients.common

import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.URLBuilder
import org.slf4j.Logger

fun createClientLogging(name: String, logger: Logger) : ClientPlugin<Unit> = createClientPlugin(name) {
    onRequest { request, _ ->
        val cleanUrl = URLBuilder(request.url).apply { parameters.clear() }.buildString()
        logger.info("[Outgoing Request] HTTP Request: [${request.method}] $cleanUrl")
    }
    onResponse { response ->
        val cleanUrl = URLBuilder(response.call.request.url).apply { parameters.clear() }.buildString()
        logger.info("[Outgoing Request] HTTP Response: [${response.status.value}] [${response.status.description}] for $cleanUrl")
    }
}
