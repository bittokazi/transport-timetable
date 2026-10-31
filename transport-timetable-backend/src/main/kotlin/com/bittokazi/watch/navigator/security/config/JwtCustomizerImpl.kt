package com.bittokazi.watch.navigator.security.config

import com.bittokazi.ktor.auth.domains.token.TokenType
import com.bittokazi.ktor.auth.services.JwtTokenCustomizer
import com.bittokazi.ktor.auth.services.providers.OAuthClientDTO
import com.bittokazi.watch.navigator.persistence.entity.RoleEnum
import com.bittokazi.watch.navigator.persistence.repository.UserRepository
import com.bittokazi.watch.navigator.security.entity.RoleScopeConfiguration
import com.nimbusds.jwt.JWTClaimsSet
import io.ktor.server.application.ApplicationCall
import kotlin.collections.joinToString

class JwtCustomizerImpl(
    private val userRepository: UserRepository,
) : JwtTokenCustomizer {
    override fun customize(
        user: String?,
        client: OAuthClientDTO?,
        claims: JWTClaimsSet.Builder,
        call: ApplicationCall?,
        params: Map<String, String?>,
        tokenType: TokenType,
    ): Map<String, Any> {
        if (user != null) {
            val userDto = userRepository.findOneById(user!!.toLong())

            var scopes =
                userDto?.roles
                    ?.get(0)?.roleKey?.let { roleKey ->
                        RoleScopeConfiguration.roles[RoleEnum.valueOf(roleKey)]?.let {
                            it.joinToString(" ") { roleKey -> roleKey.name }
                        } ?: ""
                    } ?: ""

            if (scopes != "") scopes = " $scopes"

            return mapOf<String, Any>(
                "scope" to ("${claims.claims["scope"]}$scopes"),
                "username" to userDto!!.email,
            )
        }

        return emptyMap()
    }
}
