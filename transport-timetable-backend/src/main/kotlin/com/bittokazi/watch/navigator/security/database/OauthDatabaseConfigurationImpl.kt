package com.bittokazi.watch.navigator.security.database

import com.bittokazi.ktor.auth.database.OauthDatabaseConfiguration
import com.bittokazi.watch.navigator.database.config.MultiTenantDatabaseConfiguration
import io.ktor.server.application.ApplicationCall
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class OauthDatabaseConfigurationImpl(
    val multiTenantDatabaseConfiguration: MultiTenantDatabaseConfiguration,
) : OauthDatabaseConfiguration {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    init {
        log.info("[OauthDatabaseConfiguration] -> init")
    }

    override fun <T> dbQuery(
        call: ApplicationCall?,
        block: () -> T,
    ): T {
        return transaction(multiTenantDatabaseConfiguration.getTenantDatabase("default", "public")) { block() }
    }
}
