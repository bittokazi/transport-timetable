package com.bittokazi.watch.navigator.clients.transit

import com.bittokazi.watch.navigator.clients.common.createClientLogging
import com.bittokazi.watch.navigator.persistence.entity.CallResult
import com.bittokazi.watch.navigator.persistence.entity.MotisItineraryResponse
import com.bittokazi.watch.navigator.persistence.entity.MotisLocation
import com.bittokazi.watch.navigator.persistence.entity.PlaceResponse
import com.bittokazi.watch.navigator.persistence.entity.Station
import com.bittokazi.watch.navigator.persistence.entity.Stop
import com.bittokazi.watch.navigator.persistence.entity.StopResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.encodeURLPath
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.plugins.di.annotations.Property
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.format.DateTimeParseException

class TransitClient(
    val transitClientConfig: TransitClientConfig,
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
        log.info("[TransitClient] -> created")
    }

    suspend fun search(query: String): CallResult<List<PlaceResponse>, TransitClientErrorCode> {
        return try {
            val encodedQuery = query.encodeURLPath()
            val url = "${transitClientConfig.baseUrl}/geocode?text=$encodedQuery"
            val response = httpClient.get(url) {
                header(HttpHeaders.UserAgent, "Transport Timetable, v1, bitto.kazi@gmail.com")
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val data: List<MotisLocation> = response.body()
                    val results = data.map { mapLocationToOTP(it) }
                    CallResult.Success(results)
                }
                else -> {
                    log.error("Transit search failed with status: ${response.status}")
                    CallResult.Failure(TransitClientErrorCode.SERVICE_ERROR)
                }
            }
        } catch (e: Exception) {
            log.error("Transit search error:", e)
            CallResult.Failure(TransitClientErrorCode.REQUEST_ERROR, cause = e)
        }
    }

    suspend fun plan(
        fromPlace: String,
        toPlace: String,
        numItineraries: Int = 5,
        time: String? = null,
        arriveBy: String? = null,
    ): CallResult<MotisItineraryResponse, TransitClientErrorCode> {
        return try {
            val encodedFrom = fromPlace.encodeURLPath()
            val encodedTo = toPlace.encodeURLPath()
            val sanitizedTime = sanitizeTimeParam(time)

            var url =
                "${transitClientConfig.baseUrl}/plan" +
                    "?fromPlace=$encodedFrom" +
                    "&toPlace=$encodedTo" +
                    "&numItineraries=$numItineraries"

            if (!sanitizedTime.isNullOrEmpty()) {
                url += "&time=${sanitizedTime.encodeURLPath()}"
            }

            if (!arriveBy.isNullOrEmpty()) {
                url += "&arriveBy=$arriveBy"
            }

            log.info("MOTIS PLAN URL: $url")

            val response = httpClient.get(url) {
                header(HttpHeaders.UserAgent, "Transport Timetable, v1, bitto.kazi@gmail.com")
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val data: MotisItineraryResponse = response.body()
                    CallResult.Success(data)
                }
                else -> {
                    log.error("Transit plan failed with status: ${response.status}, {}", response.body<String>())
                    CallResult.Failure(TransitClientErrorCode.SERVICE_ERROR)
                }
            }
        } catch (e: Exception) {
            log.error("Transit plan error:", e)
            CallResult.Failure(TransitClientErrorCode.REQUEST_ERROR, cause = e)
        }
    }

    private fun sanitizeTimeParam(time: String?): String? {
        if (time.isNullOrBlank()) {
            return null
        }

        return try {
            Instant.parse(time)
            time
        } catch (e: DateTimeParseException) {
            log.warn("Ignoring invalid transit time parameter: {}. Reason: {}", time, e.message)
            null
        }
    }

    private fun mapLocationToOTP(loc: MotisLocation): PlaceResponse {
        var lat: Double?
        var lon: Double?

        val id: String?
        val name: String?

        when (val station = loc.station) {
            is Station -> {
                lat = station.latitude
                    ?: station.coord?.lat

                lon = station.longitude
                    ?: station.coord?.lon

                id = station.id
                name = station.name ?: "Unknown"
            }

            else ->
                when (val stop = loc.stop) {
                    is Stop -> {
                        lat = stop.latitude
                            ?: stop.coord?.lat

                        lon = stop.longitude
                            ?: stop.coord?.lon

                        id = stop.id
                        name = stop.name ?: "Unknown"
                    }
                    else -> {
                        lat = loc.latitude
                            ?: loc.lat

                        lon = loc.longitude
                            ?: loc.lon

                        id = loc.id
                        name = loc.name ?: "Unknown"
                    }
                }
        }

        return PlaceResponse(
            id = id,
            name = name,
            lat = lat,
            lon = lon,
            stops =
                listOf(
                    StopResponse(
                        gtfsId = id,
                        name = name,
                        code = id,
                        lat = lat,
                        lon = lon,
                    ),
                ),
        )
    }
}

data class TransitClientConfig(
    @Property("transit-client-config.enabled") val enabled: Boolean,
    @Property("transit-client-config.base-url") val baseUrl: String,
)

enum class TransitClientErrorCode {
    SERVICE_ERROR,
    REQUEST_ERROR,
    INVALID_LOCATION,
}
