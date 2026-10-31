package com.bittokazi.watch.navigator.app

import com.bittokazi.watch.navigator.app.config.AppModule
import com.bittokazi.watch.navigator.app.config.AppModuleConfiguration
import com.bittokazi.watch.navigator.app.cpanel.cpanelModule
import com.bittokazi.watch.navigator.app.feedback.feedbackModule
import com.bittokazi.watch.navigator.app.password.reset.passwordResetModule
import com.bittokazi.watch.navigator.app.role.roleModule
import com.bittokazi.watch.navigator.app.tenant.infoModule
import com.bittokazi.watch.navigator.app.transport.transportModule
import com.bittokazi.watch.navigator.app.user.userModule
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*

fun Application.configureAppModule() {
    dependencies {
        provide(AppModuleConfiguration::class)
    }

    val appModuleConfiguration: AppModuleConfiguration by dependencies

    val appInfo = infoModule()

    if (appModuleConfiguration.modules[AppModule.USER]?.enabled == true) {
        userModule()
    }
    if (appModuleConfiguration.modules[AppModule.CPANEL]?.enabled == true) {
        cpanelModule()
    }
    if (appModuleConfiguration.modules[AppModule.TRANSPORT]?.enabled == true) {
        transportModule()
    }
    if (appModuleConfiguration.modules[AppModule.PASSWORD_RESET]?.enabled == true) {
        passwordResetModule()
    }
    if (appModuleConfiguration.modules[AppModule.ROLE]?.enabled == true) {
        roleModule()
    }
    if (appModuleConfiguration.modules[AppModule.FEEDBACK]?.enabled == true) {
        feedbackModule()
    }

    log.info("[AppModule] -> init with version: ${appInfo.version}")
}
