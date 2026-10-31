package com.bittokazi.watch.navigator.persistence.repository

import com.bittokazi.watch.navigator.database.config.MultiTenantDatabaseConfiguration
import com.bittokazi.watch.navigator.persistence.entity.PasswordResetToken
import com.bittokazi.watch.navigator.persistence.entity.PasswordResetTokenData
import com.bittokazi.watch.navigator.persistence.entity.PasswordResetTokens
import com.bittokazi.watch.navigator.persistence.entity.PasswordResetTokens.expireDate
import com.bittokazi.watch.navigator.persistence.entity.PasswordResetTokens.token
import com.bittokazi.watch.navigator.persistence.entity.User
import com.bittokazi.watch.navigator.persistence.entity.UserData
import korlibs.time.hours
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalTime::class)
class PasswordResetTokenRepository(
    multiTenantDatabaseConfiguration: MultiTenantDatabaseConfiguration,
) : BaseRepository(multiTenantDatabaseConfiguration) {
    init {
        log.info("[PasswordResetTokenRepository] -> created")
    }

    @OptIn(ExperimentalUuidApi::class)
    fun create(userId: Long): PasswordResetTokenData {
        return query {
            PasswordResetToken.new {
                token = Uuid.random().toString().replace("-", "")
                user = User.findById(userId)!!
                expireDate = Clock.System.now() + 2.hours
            }.toDto()
        }
    }

    fun findByToken(token: String): PasswordResetTokenData? {
        return query {
            PasswordResetToken.find { PasswordResetTokens.token eq token }.singleOrNull()?.toDto()
        }
    }

    fun delete(userData: UserData): Int {
        return query {
            PasswordResetTokens.deleteWhere { PasswordResetTokens.user eq userData.id!! }
        }
    }
}
