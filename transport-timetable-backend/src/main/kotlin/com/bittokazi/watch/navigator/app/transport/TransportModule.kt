package com.bittokazi.watch.navigator.app.transport

import com.bittokazi.watch.navigator.app.transport.controllers.transportRoute
import com.bittokazi.watch.navigator.app.transport.services.DefaultTransportService
import com.bittokazi.watch.navigator.app.transport.services.TransportService
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies

fun Application.transportModule() {
    dependencies {
        provide<TransportService>(DefaultTransportService::class)
    }

    transportRoute()
}
