package com.bittokazi.watch.navigator.app.role.controllers

import com.bittokazi.watch.navigator.app.role.services.RoleService
import com.bittokazi.watch.navigator.config.RouteConfig.API_ROLE
import com.bittokazi.watch.navigator.persistence.entity.ScopeEnum
import com.bittokazi.watch.navigator.security.services.scopeCheck
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun Application.roleRoutes() {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    val roleService: RoleService by dependencies

    routing {
        authenticate {
            get(API_ROLE) {
                scopeCheck(
                    listOf(
                        ScopeEnum.ROLE_READ_ALL,
                    ),
                    call,
                ) {
                    call.respond(roleService.findAll())
                }
            }
        }
    }

    log.info("[roleRoute] -> Routes configured.")
}
