package com.bittokazi.watch.navigator.app.password.reset.services

import com.bittokazi.watch.navigator.app.config.AppModuleConfiguration
import com.bittokazi.watch.navigator.mail.config.MailConfig
import com.bittokazi.watch.navigator.mail.services.MailGeneratorService
import com.bittokazi.watch.navigator.mail.services.MailService
import com.bittokazi.watch.navigator.mail.services.ResetPasswordMailLabel
import com.bittokazi.watch.navigator.persistence.entity.CallResult
import com.bittokazi.watch.navigator.persistence.entity.PasswordResetTokenData
import com.bittokazi.watch.navigator.persistence.entity.UserData
import com.bittokazi.watch.navigator.persistence.repository.PasswordResetTokenRepository
import com.bittokazi.watch.navigator.persistence.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

interface PasswordResetService {
    fun create(passwordResetTokenRequest: PasswordResetTokenRequest): CallResult<PasswordResetTokenData, PasswordResetTokenError>

    fun find(token: String): CallResult<PasswordResetTokenData, PasswordResetTokenError>

    fun reset(
        token: String,
        passwordResetRequest: PasswordResetRequest,
    ): CallResult<Boolean, PasswordResetTokenError>
}

@OptIn(ExperimentalTime::class)
class DefaultPasswordResetService(
    private val userRepository: UserRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val mailService: MailService,
    private val mailGeneratorService: MailGeneratorService,
    private val appModuleConfiguration: AppModuleConfiguration,
    private val mailConfig: MailConfig,
) : PasswordResetService {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    init {
        log.info("[PasswordResetService] -> init")
    }

    override fun create(passwordResetTokenRequest: PasswordResetTokenRequest): CallResult<PasswordResetTokenData, PasswordResetTokenError> {
        return when (val userData = userRepository.findOneByEmail(passwordResetTokenRequest.email)) {
            is UserData ->
                when (userData.id) {
                    is Long -> {
                        val result = passwordResetTokenRepository.create(userData.id)

                        CoroutineScope(Dispatchers.Default).launch {
                            mailService.sendMail(
                                result.user.email,
                                mailConfig.from,
                                "Password Reset Request",
                                mailGeneratorService.generateResetPasswordMail(
                                    mapOf(
                                        ResetPasswordMailLabel.USERNAME to "${result.user.firstName} ${result.user.lastName}",
                                        ResetPasswordMailLabel.RESET_LINK to "${appModuleConfiguration.domain}/app/reset-password/${result.token}",
                                    ),
                                ),
                            )
                        }
                        CallResult.Success(result)
                    }

                    else -> CallResult.Failure(PasswordResetTokenError.USER_NOT_FOUND)
                }

            else -> CallResult.Failure(PasswordResetTokenError.USER_NOT_FOUND)
        }
    }

    override fun find(token: String): CallResult<PasswordResetTokenData, PasswordResetTokenError> {
        return when (val passwordResetTokenData = passwordResetTokenRepository.findByToken(token)) {
            is PasswordResetTokenData ->
                when (passwordResetTokenData.expireDate < Clock.System.now()) {
                    false -> CallResult.Success(passwordResetTokenData)

                    true -> CallResult.Failure(PasswordResetTokenError.TOKEN_EXPIRED)
                }

            else -> CallResult.Failure(PasswordResetTokenError.TOKEN_NOT_FOUND)
        }
    }

    override fun reset(
        token: String,
        passwordResetRequest: PasswordResetRequest,
    ): CallResult<Boolean, PasswordResetTokenError> {
        return when (val passwordResetTokenData = passwordResetTokenRepository.findByToken(token)) {
            is PasswordResetTokenData ->
                when (passwordResetTokenData.expireDate < Clock.System.now()) {
                    false ->
                        when (passwordResetRequest.password == passwordResetRequest.reTypedPassword) {
                            true ->
                                when (
                                    val user =
                                        userRepository.resetPassword(
                                            passwordResetTokenData.user.id!!,
                                            passwordResetRequest.password,
                                        )
                                ) {
                                    is UserData -> {
                                        passwordResetTokenRepository.delete(user)
                                        CallResult.Success(true)
                                    }

                                    else -> CallResult.Failure(PasswordResetTokenError.INTERNAL_ERROR)
                                }

                            else -> CallResult.Failure(PasswordResetTokenError.PASSWORD_MISMATCH)
                        }

                    true -> CallResult.Failure(PasswordResetTokenError.TOKEN_EXPIRED)
                }

            else -> CallResult.Failure(PasswordResetTokenError.TOKEN_NOT_FOUND)
        }
    }
}

enum class PasswordResetTokenError {
    USER_NOT_FOUND,
    TOKEN_NOT_FOUND,
    TOKEN_EXPIRED,
    PASSWORD_MISMATCH,
    INTERNAL_ERROR,
}

@Serializable
data class PasswordResetTokenRequest(
    val email: String,
)

@Serializable
data class PasswordResetRequest(
    val password: String,
    val reTypedPassword: String,
)
