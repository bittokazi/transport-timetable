package com.bittokazi.watch.navigator.app.transport.services

import com.bittokazi.watch.navigator.clients.maps.GoogleMapsStaticClient
import com.bittokazi.watch.navigator.clients.transit.TransitClient
import com.bittokazi.watch.navigator.persistence.entity.CallResult
import com.bittokazi.watch.navigator.persistence.entity.Itinerary
import com.bittokazi.watch.navigator.persistence.entity.Leg
import com.bittokazi.watch.navigator.persistence.entity.LegPlace
import com.bittokazi.watch.navigator.persistence.entity.LegTime
import com.bittokazi.watch.navigator.persistence.entity.MotisItineraryResponse
import com.bittokazi.watch.navigator.persistence.entity.PlaceRequest
import com.bittokazi.watch.navigator.persistence.entity.PlaceResponse
import com.bittokazi.watch.navigator.persistence.entity.Plan
import com.bittokazi.watch.navigator.persistence.entity.PlanData
import com.bittokazi.watch.navigator.persistence.entity.PlanResponse
import com.bittokazi.watch.navigator.persistence.entity.Route
import com.bittokazi.watch.navigator.persistence.entity.StopInfo
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Instant

interface TransportService {
    suspend fun search(query: String): CallResult<List<PlaceResponse>, TransportServiceError>

    suspend fun plan(
        from: String,
        to: String,
        time: String?,
        arriveBy: String?,
    ): CallResult<PlanResponse, TransportServiceError>

    suspend fun getStaticMap(queryString: String): CallResult<ByteArray, TransportServiceError>
}

class DefaultTransportService(
    val transitClient: TransitClient,
    val googleMapsClient: GoogleMapsStaticClient,
) : TransportService {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    init {
        log.info("[TransportService] -> init")
    }

    override suspend fun search(query: String): CallResult<List<PlaceResponse>, TransportServiceError> {
        return when (val result = transitClient.search(query)) {
            is CallResult.Success -> CallResult.Success(result.outcome)
            is CallResult.Failure -> {
                log.error("Search failed: ${result.errorCode}")
                CallResult.Failure(TransportServiceError.SEARCH_FAILED)
            }
        }
    }

    override suspend fun plan(
        from: String,
        to: String,
        time: String?,
        arriveBy: String?,
    ): CallResult<PlanResponse, TransportServiceError> {
        return try {
            val formattedFrom = resolvePlace(from)
            val formattedTo = resolvePlace(to)

            when (val result = transitClient.plan(formattedFrom, formattedTo, 5, time, arriveBy)) {
                is CallResult.Success -> {
                    val motisResponse = result.outcome
                    val planResponse = mapMotisResponseToOTP(motisResponse)
                    CallResult.Success(planResponse)
                }
                is CallResult.Failure -> {
                    log.error("Plan failed: ${result.errorCode}")
                    CallResult.Failure(TransportServiceError.PLAN_FAILED)
                }
            }
        } catch (e: Exception) {
            log.error("Plan error:", e)
            CallResult.Failure(TransportServiceError.PLAN_FAILED, cause = e)
        }
    }

    override suspend fun getStaticMap(queryString: String): CallResult<ByteArray, TransportServiceError> {
        return when (val result = googleMapsClient.getStaticMap(queryString)) {
            is CallResult.Success -> CallResult.Success(result.outcome)
            is CallResult.Failure -> {
                log.error("Map proxy failed: ${result.errorCode}")
                CallResult.Failure(TransportServiceError.MAP_FAILED)
            }
        }
    }

    private fun resolvePlace(placeParam: String): String {
        return try {
            val json = Json { ignoreUnknownKeys = true }
            val parsed = json.decodeFromString<PlaceRequest>(placeParam)

            // If the ID exists and isn't a blank empty string, use it
            if (!parsed.id.isNullOrBlank()) {
                return parsed.id
            }

            // Fallback: If ID is empty, format as "lat,lon" string
            if (parsed.lat != null && parsed.lon != null) {
                return "${parsed.lat},${parsed.lon}"
            }

            // Return raw param if properties aren't structured as expected
            placeParam
        } catch (e: Exception) {
            // If it's already a raw string instead of a serialized object, use it directly
            placeParam
        }
    }

    private fun mapMotisResponseToOTP(response: MotisItineraryResponse): PlanResponse {
        val itineraries =
            response.itineraries?.map { motisIt ->
                Itinerary(
                    start = motisIt.startTime,
                    end = motisIt.endTime,
                    transfers = motisIt.transfers,
                    legs =
                        (motisIt.legs ?: emptyList()).map { leg ->

                            // Convert ISO strings safely to unix epoch milliseconds or seconds
                            val startEpoch =
                                try {
                                    leg.scheduledStartTime?.let { Instant.parse(it).toEpochMilli() }
                                        ?: leg.startTime?.let { Instant.parse(it).toEpochMilli() }
                                } catch (e: Exception) {
                                    null
                                }

                            val endEpoch =
                                try {
                                    leg.scheduledEndTime?.let { Instant.parse(it).toEpochMilli() }
                                        ?: leg.endTime?.let { Instant.parse(it).toEpochMilli() }
                                } catch (e: Exception) {
                                    null
                                }

                            Leg(
                                mode = leg.mode.uppercase(),
                                start = LegTime(scheduledTime = startEpoch),
                                end = LegTime(scheduledTime = endEpoch),
                                from =
                                    LegPlace(
                                        name = leg.from?.name,
                                        stop = leg.from?.stopId?.let { StopInfo(code = it) },
                                        lat = leg.from?.lat,
                                        lon = leg.from?.lon,
                                    ),
                                to =
                                    LegPlace(
                                        name = leg.to?.name,
                                        stop = leg.to?.stopId?.let { StopInfo(code = it) },
                                        lat = leg.to?.lat,
                                        lon = leg.to?.lon,
                                    ),
                                headsign = leg.headsign,
                                route =
                                    Route(
                                        shortName = leg.routeShortName,
                                        longName = leg.routeLongName ?: "",
                                    ),
                                distance = leg.distance,
                            )
                        },
                )
            } ?: emptyList()

        return PlanResponse(
            data =
                PlanData(
                    plan = Plan(itineraries = itineraries),
                ),
        )
    }
}

enum class TransportServiceError {
    SEARCH_FAILED,
    PLAN_FAILED,
    MAP_FAILED,
}
