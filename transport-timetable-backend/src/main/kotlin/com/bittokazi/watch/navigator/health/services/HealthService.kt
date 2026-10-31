package com.bittokazi.watch.navigator.health.services

import com.bittokazi.watch.navigator.persistence.entity.CallResult
import io.ktor.server.application.*

interface HealthService {
    fun checkHealth(call: ApplicationCall): CallResult<HealthCheckResponse, HealthServiceErrorCode>
}

data class HealthCheckResponse(
    val status: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
)

enum class HealthServiceErrorCode {
    DATABASE_UNAVAILABLE,
    HEALTH_CHECK_FAILED,
}
