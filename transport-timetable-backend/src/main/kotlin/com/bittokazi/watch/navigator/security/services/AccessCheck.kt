package com.bittokazi.watch.navigator.security.services

import com.bittokazi.watch.navigator.persistence.entity.RoleEnum
import com.bittokazi.watch.navigator.persistence.entity.ScopeEnum
import com.bittokazi.watch.navigator.security.entity.RoleScopeConfiguration
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

suspend fun scopeCheck(
    scopes: List<ScopeEnum>,
    call: ApplicationCall,
    block: suspend () -> Unit,
) {
    val errorResponse =
        suspend {
            call.respond(
                status = HttpStatusCode.Forbidden,
                message =
                    mapOf(
                        "error" to "forbidden",
                        "status" to "FORBIDDEN/403",
                        "message" to "Access denied!",
                    ),
            )
        }

    val result =
        runCatching {
            var actorScopes = emptyList<String>()

            call.principal<JWTPrincipal>()?.let { principal ->
                actorScopes = principal.payload.claims["scope"].toString().replace("\"", "").split(" ")
            }

            when (val r = scopes.filter {
                actorScopes.contains(it.name)
            }.size) {
                0 -> when (actorScopes.contains(ScopeEnum.ROLE_USER.name)) {
                    true -> scopes.filter {
                        RoleScopeConfiguration.roles[RoleEnum.ROLE_USER]?.contains(ScopeEnum.valueOf(it.name)) ?: false
                    }.size

                    false -> 0
                }

                else -> r
            }
        }

    when {
        result.isSuccess ->
            when {
                result.getOrNull()!! > 0 -> block()
                else -> errorResponse()
            }
        else -> errorResponse()
    }
}
