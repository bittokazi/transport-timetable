package com.bittokazi.watch.navigator.app.user.services

import at.favre.lib.crypto.bcrypt.BCrypt
import com.bittokazi.watch.navigator.mail.config.MailConfig
import com.bittokazi.watch.navigator.mail.services.MailGeneratorService
import com.bittokazi.watch.navigator.mail.services.MailService
import com.bittokazi.watch.navigator.mail.services.WelcomeEmailLabel
import com.bittokazi.watch.navigator.persistence.entity.CallResult
import com.bittokazi.watch.navigator.persistence.entity.PaginatedResponse
import com.bittokazi.watch.navigator.persistence.entity.PaginationRequest
import com.bittokazi.watch.navigator.persistence.entity.RoleEnum
import com.bittokazi.watch.navigator.persistence.entity.UserData
import com.bittokazi.watch.navigator.persistence.repository.RoleRepository
import com.bittokazi.watch.navigator.persistence.repository.UserRepository
import com.bittokazi.watch.navigator.validation.config.ValidationConfiguration
import com.bittokazi.watch.navigator.validation.services.ValidationService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface UserService {
    fun whoAmI(call: ApplicationCall): CallResult<UserData, UserServiceError>

    fun register(
        call: ApplicationCall,
        userData: UserData,
        signUp: Boolean,
    ): CallResult<UserData, UserServiceError>

    fun get(call: ApplicationCall): CallResult<UserData, UserServiceError>

    fun getAll(paginationRequest: PaginationRequest): PaginatedResponse<UserData>

    fun update(
        call: ApplicationCall,
        userData: UserData,
        self: Boolean,
    ): CallResult<UserData, UserServiceError>

    fun updatePassword(
        call: ApplicationCall,
        userData: UserData,
        self: Boolean,
    ): CallResult<UserData, UserServiceError>
}

class DefaultUserService(
    val userRepository: UserRepository,
    val roleRepository: RoleRepository,
    val mailGeneratorService: MailGeneratorService,
    val mailService: MailService,
    val mailConfig: MailConfig,
    val validationConfiguration: ValidationConfiguration,
    val validationService: ValidationService,
) : UserService {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    init {
        log.info("[UserService] -> init")
    }

    override fun whoAmI(call: ApplicationCall): CallResult<UserData, UserServiceError> {
        return when (val principal = call.principal<JWTPrincipal>()) {
            is JWTPrincipal -> {
                val userData =
                    userRepository.findOneById(
                        principal.subject?.toLongOrNull() ?: -1,
                    )

                if (userData?.roles?.find { it.roleKey == RoleEnum.ROLE_USER.name } != null) {
                    when (val usageCount = validationService.getCount(userData.id!!)) {
                        is CallResult.Success -> userData.usageCount = usageCount.outcome
                        else -> userData.usageCount = 0
                    }
                }

                when (userData) {
                    is UserData -> CallResult.Success(userData)
                    else -> CallResult.Failure(UserServiceError.NOT_FOUND)
                }
            }
            else -> {
                CallResult.Failure(UserServiceError.NOT_FOUND)
            }
        }
    }

    override fun register(
        call: ApplicationCall,
        userData: UserData,
        signUp: Boolean,
    ): CallResult<UserData, UserServiceError> {
        return when (userRepository.findOneByEmail(userData.email)) {
            is UserData -> CallResult.Failure(UserServiceError.EMAIL_EXIST)

            else -> {
                if (signUp) {
                    val role = roleRepository.findOneByKey(RoleEnum.ROLE_USER)

                    userData.roles =
                        listOf(
                            role!!,
                        )
                }

                val inserted = userRepository.insert(userData)

                userData.roles
                    ?.find { roleDto -> roleDto.roleKey == RoleEnum.ROLE_USER.toString() }
                    ?.let {
                        CoroutineScope(Dispatchers.Default).launch {
                            mailService.sendMail(
                                inserted.email,
                                mailConfig.from,
                                "Welcome to Zepp Transport Timetable App",
                                mailGeneratorService.generateWelcomeEmail(
                                    mapOf(
                                        WelcomeEmailLabel.USERNAME
                                                to "${inserted.firstName} ${inserted.lastName}",
                                        WelcomeEmailLabel.LIMIT
                                                to "${validationConfiguration.limit}",
                                    ),
                                ),
                            )
                        }
                    }

                CallResult.Success(inserted)
            }
        }
    }

    override fun get(call: ApplicationCall): CallResult<UserData, UserServiceError> {
        return when (val existing = userRepository.findOneById(call.parameters["id"]?.toLongOrNull() ?: -1)) {
            is UserData -> CallResult.Success(existing)

            else -> CallResult.Failure(UserServiceError.NOT_FOUND)
        }
    }

    override fun getAll(paginationRequest: PaginationRequest): PaginatedResponse<UserData> {
        return userRepository.getAll(paginationRequest)
    }

    override fun update(
        call: ApplicationCall,
        userData: UserData,
        self: Boolean,
    ): CallResult<UserData, UserServiceError> {
        return when (userData.id) {
            is Long -> {
                when (self) {
                    true -> updateOwnProfile(call, userData)

                    false -> {
                        when (val existing = userRepository.findOneById(userData.id)) {
                            is UserData -> {
                                when (existing.email == userData.email) {
                                    true ->
                                        CallResult.Success(
                                            userRepository.updateProfile(userData.id, userData)!!,
                                        )

                                    false ->
                                        when (userRepository.findOneByEmail(userData.email)) {
                                            is UserData -> CallResult.Failure(UserServiceError.EMAIL_EXIST)
                                            else ->
                                                CallResult.Success(
                                                    userRepository.updateProfile(userData.id, userData)!!,
                                                )
                                        }
                                }
                            }

                            else -> CallResult.Failure(UserServiceError.NOT_FOUND)
                        }
                    }
                }
            }

            else -> CallResult.Failure(UserServiceError.UNPROCESSABLE_ENTITY)
        }
    }

    override fun updatePassword(
        call: ApplicationCall,
        userData: UserData,
        self: Boolean,
    ): CallResult<UserData, UserServiceError> {
        return when (userData.id) {
            is Long -> {
                when (self) {
                    true -> updateOwnPassword(call, userData)

                    false -> {
                        when (val existing = userRepository.findOneById(userData.id, true)) {
                            is UserData ->
                                CallResult.Success(
                                    userRepository.resetPassword(existing.id!!, userData.newPassword!!)!!,
                                )

                            else -> CallResult.Failure(UserServiceError.NOT_FOUND)
                        }
                    }
                }
            }

            else -> CallResult.Failure(UserServiceError.UNPROCESSABLE_ENTITY)
        }
    }

    private fun updateOwnProfile(
        call: ApplicationCall,
        userData: UserData,
    ): CallResult<UserData, UserServiceError> {
        return when (val principal = call.principal<JWTPrincipal>()) {
            is JWTPrincipal -> {
                when (val existing = userRepository.findOneById(principal.subject?.toLongOrNull() ?: -1)) {
                    is UserData ->
                        when (existing.id == userData.id) {
                            true -> when (existing.email == userData.email) {
                                true -> CallResult.Success(
                                    userRepository.updateProfile(existing.id!!, userData)!!,
                                )

                                false ->  when (userRepository.findOneByEmail(userData.email)) {
                                    is UserData -> CallResult.Failure(UserServiceError.EMAIL_EXIST)
                                    else ->
                                        CallResult.Success(
                                            userRepository.updateProfile(existing.id!!, userData)!!,
                                        )
                                }
                            }

                            false -> CallResult.Failure(UserServiceError.UNPROCESSABLE_ENTITY)
                        }

                    else -> CallResult.Failure(UserServiceError.NOT_FOUND)
                }
            }

            else -> {
                CallResult.Failure(UserServiceError.NOT_FOUND)
            }
        }
    }

    private fun updateOwnPassword(
        call: ApplicationCall,
        userData: UserData,
    ): CallResult<UserData, UserServiceError> {
        return when (val principal = call.principal<JWTPrincipal>()) {
            is JWTPrincipal -> {
                when (val existing = userRepository.findOneById(principal.subject?.toLongOrNull() ?: -1, true)) {
                    is UserData ->
                        when (BCrypt.verifyer().verify(userData.password.toCharArray(), existing.password).verified) {
                            true ->
                                CallResult.Success(
                                    userRepository.resetPassword(existing.id!!, userData.newPassword!!)!!,
                                )

                            false -> CallResult.Failure(UserServiceError.PASSWORD_DOES_NOT_MATCH)
                        }

                    else -> CallResult.Failure(UserServiceError.NOT_FOUND)
                }
            }

            else -> {
                CallResult.Failure(UserServiceError.NOT_FOUND)
            }
        }
    }
}

enum class UserServiceError {
    NOT_FOUND,
    EMAIL_EXIST,
    UNPROCESSABLE_ENTITY,
    PASSWORD_DOES_NOT_MATCH,
}
