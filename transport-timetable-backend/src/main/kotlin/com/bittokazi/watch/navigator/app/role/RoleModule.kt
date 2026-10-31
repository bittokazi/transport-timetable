package com.bittokazi.watch.navigator.app.role

import com.bittokazi.watch.navigator.app.role.controllers.roleRoutes
import com.bittokazi.watch.navigator.app.role.services.DefaultRoleService
import com.bittokazi.watch.navigator.app.role.services.RoleService
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies

fun Application.roleModule() {
    dependencies {
        provide<RoleService>(DefaultRoleService::class)
    }

    roleRoutes()
}
