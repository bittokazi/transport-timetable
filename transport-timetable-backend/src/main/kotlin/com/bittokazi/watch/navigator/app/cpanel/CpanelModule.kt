package com.bittokazi.watch.navigator.app.cpanel

import com.bittokazi.watch.navigator.app.cpanel.controllers.cpanelRoutes
import io.ktor.server.application.*

fun Application.cpanelModule() {
    cpanelRoutes()
}
