package com.bittokazi.watch.navigator.validation

import com.bittokazi.watch.navigator.validation.config.ValidationConfiguration
import com.bittokazi.watch.navigator.validation.services.DefaultValidationService
import com.bittokazi.watch.navigator.validation.services.ValidationService
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies

fun Application.configureValidationModule() {
    dependencies {
        provide(ValidationConfiguration::class)
        provide<ValidationService>(DefaultValidationService::class)
    }
}
