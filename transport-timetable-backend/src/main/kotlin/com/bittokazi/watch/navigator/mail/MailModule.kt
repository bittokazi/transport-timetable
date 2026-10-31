package com.bittokazi.watch.navigator.mail

import com.bittokazi.watch.navigator.mail.config.MailConfig
import com.bittokazi.watch.navigator.mail.services.DefaultMailService
import com.bittokazi.watch.navigator.mail.services.MailGeneratorService
import com.bittokazi.watch.navigator.mail.services.MailService
import com.bittokazi.watch.navigator.mail.services.NoopMailService
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies

fun Application.configureMailModule() {
    dependencies {
        provide(MailConfig::class)
        provide(MailGeneratorService::class)
    }

    val mailConfig: MailConfig by dependencies

    dependencies {
        provide<MailService>(
            when (mailConfig.enabled) {
                true -> DefaultMailService::class

                false -> NoopMailService::class
            },
        )
    }
}
