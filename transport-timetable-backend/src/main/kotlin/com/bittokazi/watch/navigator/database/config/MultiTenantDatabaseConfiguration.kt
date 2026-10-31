package com.bittokazi.watch.navigator.database.config

import org.jetbrains.exposed.v1.jdbc.Database

interface MultiTenantDatabaseConfiguration {
    fun init()

    fun getTenantDatabase(
        key: String,
        schema: String,
    ): Database
}
