package com.bittokazi.watch.navigator.app.user

import com.bittokazi.watch.navigator.app.user.controllers.userRoute
import com.bittokazi.watch.navigator.app.user.services.DefaultUserService
import com.bittokazi.watch.navigator.app.user.services.UserService
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies

fun Application.userModule() {
    dependencies {
        provide<UserService>(DefaultUserService::class)
    }

    userRoute()
}
