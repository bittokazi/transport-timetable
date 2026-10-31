package com.bittokazi.watch.navigator.clients

import com.bittokazi.watch.navigator.clients.idp.IdpClient
import com.bittokazi.watch.navigator.clients.idp.IdpClientConfig
import com.bittokazi.watch.navigator.clients.maps.GoogleMapsConfig
import com.bittokazi.watch.navigator.clients.maps.GoogleMapsStaticClient
import com.bittokazi.watch.navigator.clients.transit.TransitClient
import com.bittokazi.watch.navigator.clients.transit.TransitClientConfig
import com.bittokazi.watch.navigator.security.controllers.configureIdpLoginRoutes
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies

fun Application.configureClientModule() {
    dependencies {
        provide(IdpClientConfig::class)
        provide(TransitClientConfig::class)
        provide(GoogleMapsConfig::class)
    }

    val idpClientConfig: IdpClientConfig by dependencies
    val transitClientConfig: TransitClientConfig by dependencies
    val googleMapsConfig: GoogleMapsConfig by dependencies

    dependencies {
        if (idpClientConfig.enabled) {
            provide(IdpClient::class)
        }

        if (transitClientConfig.enabled) {
            provide(TransitClient::class)
        }

        if (googleMapsConfig.enabled) {
            provide(GoogleMapsStaticClient::class)
        }
    }

    if (idpClientConfig.enabled) {
        configureIdpLoginRoutes()
    }
}
