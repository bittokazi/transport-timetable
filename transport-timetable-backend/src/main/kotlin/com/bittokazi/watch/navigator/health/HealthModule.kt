package com.bittokazi.watch.navigator.health

import com.bittokazi.watch.navigator.health.controllers.healthRoutes
import com.bittokazi.watch.navigator.health.services.DefaultHealthService
import com.bittokazi.watch.navigator.health.services.HealthService
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies

fun Application.configureHealthModule() {
    dependencies {
        provide<HealthService>(DefaultHealthService::class)
    }

    healthRoutes()
}
