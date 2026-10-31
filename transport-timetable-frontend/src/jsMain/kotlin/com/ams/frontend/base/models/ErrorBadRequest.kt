package com.ams.frontend.base.models

import kotlinx.serialization.Serializable

@Serializable
data class ErrorBadRequest(
    val message: String? = null,
    val errors: Map<String, String>? = mapOf(),
    val status: String? = null,
)
