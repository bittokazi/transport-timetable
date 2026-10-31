package com.bittokazi.watch.navigator.app.password.reset

import com.bittokazi.watch.navigator.app.password.reset.controllers.passwordResetRoutes
import com.bittokazi.watch.navigator.app.password.reset.services.DefaultPasswordResetService
import com.bittokazi.watch.navigator.app.password.reset.services.PasswordResetService
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies

fun Application.passwordResetModule() {
    dependencies {
        provide<PasswordResetService>(DefaultPasswordResetService::class)
    }

    passwordResetRoutes()
}
