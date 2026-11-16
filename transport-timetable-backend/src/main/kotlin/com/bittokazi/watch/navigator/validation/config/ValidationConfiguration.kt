package com.bittokazi.watch.navigator.validation.config

import io.ktor.server.plugins.di.annotations.Property

data class ValidationConfiguration(
    @Property("validation-config.limit") val limit: Long,
    @Property("validation-config.version.enabled") val versionEnabled: Boolean,
    @Property("validation-config.version.min") val minVersion: String?,
)
