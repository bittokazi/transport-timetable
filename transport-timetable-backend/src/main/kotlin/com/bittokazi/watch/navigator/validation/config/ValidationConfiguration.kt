package com.bittokazi.watch.navigator.validation.config

import io.ktor.server.plugins.di.annotations.Property

data class ValidationConfiguration(
    @Property("validation-config.limit") val limit: Long,
)
