package com.bittokazi.watch.navigator.security.services

import com.bittokazi.ktor.auth.services.providers.OAuthUserDTO
import com.bittokazi.ktor.auth.services.providers.OauthUserService
import com.bittokazi.watch.navigator.persistence.repository.UserRepository
import io.ktor.server.application.ApplicationCall
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class OauthUserServiceImpl(
    private val userRepository: UserRepository,
) : OauthUserService {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    init {
        log.info("[OauthUserService] -> init")
    }

    override fun findByUsername(
        username: String,
        call: ApplicationCall,
    ): OAuthUserDTO? {
        return userRepository.findOneByEmail(
            username,
            true,
        )?.let {
            OAuthUserDTO(
                id = it.id.toString(),
                username = it.email,
                email = it.email,
                firstName = it.firstName,
                lastName = it.lastName,
                isActive = true,
                passwordHash = it.password,
            )
        }
    }

    override fun findById(
        id: String,
        call: ApplicationCall,
    ): OAuthUserDTO? {
        return userRepository.findOneById(
            id.toLong(),
            true,
        )?.let {
            OAuthUserDTO(
                id = it.id.toString(),
                username = it.email,
                email = it.email,
                firstName = it.firstName,
                lastName = it.lastName,
                isActive = true,
                passwordHash = it.password,
            )
        }
    }
}
