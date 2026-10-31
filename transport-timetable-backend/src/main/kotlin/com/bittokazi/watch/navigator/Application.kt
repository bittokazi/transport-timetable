package com.bittokazi.watch.navigator

import com.bittokazi.watch.navigator.app.configureAppModule
import com.bittokazi.watch.navigator.clients.configureClientModule
import com.bittokazi.watch.navigator.database.configureDatabase
import com.bittokazi.watch.navigator.health.configureHealthModule
import com.bittokazi.watch.navigator.mail.configureMailModule
import com.bittokazi.watch.navigator.persistence.configurePersistenceModule
import com.bittokazi.watch.navigator.security.configureSecurityModule
import com.bittokazi.watch.navigator.storage.configureStorageModule
import com.bittokazi.watch.navigator.validation.configureValidationModule
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureMonitoring()
    configureDatabase()
    configurePersistenceModule()
    configureSecurityModule()
    configureClientModule()
    configureHealthModule()
    configureValidationModule()
    configureMailModule()
    configureStorageModule()
    configureAppModule()
    configureHTTP()
}
