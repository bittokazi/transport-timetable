package com.bittokazi.watch.navigator

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import io.ktor.util.date.getTimeMillis
import org.slf4j.event.*

fun Application.configureMonitoring() {
    install(CallLogging) {
        level = Level.INFO
        format { call ->
            "[Incoming Request] " + when (val status = call.response.status() ?: "Unhandled") {
                HttpStatusCode.Found -> "${status as HttpStatusCode}: " +
                        "${call.request.toLogStringWithColors()} -> ${call.response.headers[HttpHeaders.Location]}"

                "Unhandled" -> "${status}: ${call.request.toLogStringWithColors()}"

                else -> "${status as HttpStatusCode}: ${call.request.toLogStringWithColors()}"
            }
        }
        filter { call -> call.request.path().startsWith("/") }
    }
}

internal fun ApplicationRequest.toLogStringWithColors(clock: () -> Long = { getTimeMillis() }): String =
    "${httpMethod.value} - ${path()} in ${call.processingTimeMillis(clock)}ms"
