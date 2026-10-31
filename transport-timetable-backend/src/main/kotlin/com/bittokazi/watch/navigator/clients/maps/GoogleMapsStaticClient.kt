package com.bittokazi.watch.navigator.clients.maps

import com.bittokazi.watch.navigator.clients.common.createClientLogging
import com.bittokazi.watch.navigator.persistence.entity.CallResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readBytes
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.plugins.di.annotations.Property
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class GoogleMapsStaticClient(
    val googleMapsConfig: GoogleMapsConfig,
) {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    private val httpClient: HttpClient =
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
        log.info("[GoogleMapsStaticClient] -> created")
    }

    private fun decodeQueryComponent(raw: String): String {
        var value = raw
        repeat(3) {
            val decoded = URLDecoder.decode(value, StandardCharsets.UTF_8)
            if (decoded == value) {
                return decoded
            }
            value = decoded
        }
        return value
    }

    suspend fun getStaticMap(queryString: String): CallResult<ByteArray, GoogleMapsErrorCode> {
        return try {
            if (googleMapsConfig.apiKey.isBlank()) {
                log.error("[GoogleMapsStaticClient] Missing Google Maps API key")
                return CallResult.Failure(GoogleMapsErrorCode.REQUEST_ERROR)
            }

            val params =
                queryString.removePrefix("?")
                    .split("&")
                    .filter { it.isNotEmpty() }
                    .mapNotNull { entry ->
                        val parts = entry.split("=", limit = 2)
                        if (parts.isEmpty()) return@mapNotNull null

                        val key = decodeQueryComponent(parts.first())
                        if (key == "nonce") return@mapNotNull null

                        val value = decodeQueryComponent(if (parts.size == 2) parts[1] else "")
                        key to value
                    }
                    .toMutableList()

            params += "key" to googleMapsConfig.apiKey

            val googleMapUrl =
                "https://maps.googleapis.com/maps/api/staticmap?" +
                    params.joinToString("&") { (k, v) ->
                        "${URLEncoder.encode(k, StandardCharsets.UTF_8)}=${URLEncoder.encode(v, StandardCharsets.UTF_8)}"
                    }

            val response: HttpResponse = httpClient.get(googleMapUrl)

            when {
                response.status.isSuccess() -> {
                    val imageBytes = response.readBytes()
                    CallResult.Success(imageBytes)
                }
                else -> {
                    log.error("Google Maps API failed with status: ${response.status}")
                    CallResult.Failure(GoogleMapsErrorCode.API_ERROR)
                }
            }
        } catch (e: Exception) {
            log.error("Static map proxy error:", e)
            CallResult.Failure(GoogleMapsErrorCode.REQUEST_ERROR, cause = e)
        }
    }
}

data class GoogleMapsConfig(
    @Property("google-maps-config.enabled") val enabled: Boolean,
    @Property("google-maps-config.api-key") val apiKey: String,
)

enum class GoogleMapsErrorCode {
    API_ERROR,
    REQUEST_ERROR,
}
