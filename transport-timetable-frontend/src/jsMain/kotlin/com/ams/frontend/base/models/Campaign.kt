package com.ams.frontend.base.models

import kotlinx.serialization.Serializable

@Serializable
data class Campaign(
    val topic: String,
    val title: String,
    val body: String,
)
