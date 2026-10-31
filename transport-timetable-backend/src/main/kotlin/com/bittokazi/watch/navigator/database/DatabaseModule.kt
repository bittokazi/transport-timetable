package com.bittokazi.watch.navigator.database

import com.bittokazi.watch.navigator.database.config.DatabaseConfigurationHolder
import com.bittokazi.watch.navigator.database.config.DefaultMultiTenantDatabaseConfiguration
import com.bittokazi.watch.navigator.database.config.MultiTenantDatabaseConfiguration
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies

fun Application.configureDatabase() {
    dependencies {
        provide(DatabaseConfigurationHolder::class)
        provide<MultiTenantDatabaseConfiguration>(DefaultMultiTenantDatabaseConfiguration::class)
    }

    val multiTenantDatabaseConfiguration: MultiTenantDatabaseConfiguration by dependencies

    multiTenantDatabaseConfiguration.init()
}
