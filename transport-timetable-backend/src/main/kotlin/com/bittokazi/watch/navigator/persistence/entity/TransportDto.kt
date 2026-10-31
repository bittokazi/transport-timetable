package com.bittokazi.watch.navigator.persistence.entity

import kotlinx.serialization.Serializable

// ==================================================
// Place/Location Target DTOs
// ==================================================

@Serializable
data class PlaceRequest(
    val id: String? = null,
    val lat: Double? = null,
    val lon: Double? = null,
)

@Serializable
data class PlaceResponse(
    val id: String? = null,
    val name: String = "Unknown",
    val lat: Double? = null,
    val lon: Double? = null,
    val stops: List<StopResponse>? = null,
)

@Serializable
data class StopResponse(
    val gtfsId: String? = null,
    val name: String? = null,
    val code: String? = null,
    val lat: Double? = null,
    val lon: Double? = null,
)

// ==================================================
// Journey/Itinerary Target DTOs
// ==================================================

@Serializable
data class PlanRequest(
    val fromPlace: String,
    val toPlace: String,
    val numItineraries: Int = 5,
    val time: String? = null,
    val arriveBy: String? = null,
)

@Serializable
data class PlanResponse(
    val data: PlanData,
)

@Serializable
data class PlanData(
    val plan: Plan,
)

@Serializable
data class Plan(
    val itineraries: List<Itinerary>,
)

@Serializable
data class Itinerary(
    val start: String? = null,
    val end: String? = null,
    val transfers: Int? = null,
    val legs: List<Leg>,
)

@Serializable
data class Leg(
    val mode: String = "WALK",
    val start: LegTime,
    val end: LegTime,
    val from: LegPlace,
    val to: LegPlace,
    val headsign: String? = null,
    val route: Route? = null,
    val distance: Double = 0.0,
)

@Serializable
data class LegTime(
    val scheduledTime: Long? = null,
)

@Serializable
data class LegPlace(
    val name: String? = null,
    val stop: StopInfo? = null,
    val lat: Double? = null,
    val lon: Double? = null,
)

@Serializable
data class StopInfo(
    val code: String? = null,
)

@Serializable
data class Route(
    val shortName: String? = null,
    val longName: String? = null,
)

// ==================================================
// MOTIS/Transitous API Response DTOs
// ==================================================

// UNTOUCHED: Kept exactly as original for search functionality
@Serializable
data class MotisLocation(
    val id: String? = null,
    val name: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val lat: Double? = null,
    val lon: Double? = null,
    val station: Station? = null,
    val stop: Stop? = null,
)

// UNTOUCHED: Kept exactly as original for search functionality
@Serializable
data class Station(
    val id: String? = null,
    val name: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val coord: Coordinate? = null,
)

// UNTOUCHED: Kept exactly as original for search functionality
@Serializable
data class Stop(
    val id: String? = null,
    val name: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val coord: Coordinate? = null,
)

// UNTOUCHED: Kept exactly as original for search functionality
@Serializable
data class Coordinate(
    val lat: Double? = null,
    val lon: Double? = null,
)

// FIXED: Cleaned up to explicitly parse the true JSON network payload shape
@Serializable
data class MotisItineraryResponse(
    val itineraries: List<MotisItinerary>? = null,
)

@Serializable
data class MotisItinerary(
    val duration: Int? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val transfers: Int = 0,
    val legs: List<MotisNetworkLeg>? = null,
)

@Serializable
data class MotisNetworkLeg(
    val mode: String = "WALK",
    val startTime: String? = null,
    val endTime: String? = null,
    val scheduledStartTime: String? = null,
    val scheduledEndTime: String? = null,
    val distance: Double = 0.0,
    val headsign: String? = null,
    val routeShortName: String? = null,
    val routeLongName: String? = null,
    val from: MotisNetworkPlace? = null,
    val to: MotisNetworkPlace? = null,
)

@Serializable
data class MotisNetworkPlace(
    val name: String? = null,
    val stopId: String? = null,
    val lat: Double? = null,
    val lon: Double? = null,
)
