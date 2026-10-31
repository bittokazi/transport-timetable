package com.bittokazi.watch.navigator.validation.services

import com.bittokazi.watch.navigator.persistence.entity.CallResult
import com.bittokazi.watch.navigator.persistence.entity.MonthlyCountData
import com.bittokazi.watch.navigator.persistence.repository.MonthlyCountRepository
import com.bittokazi.watch.navigator.persistence.repository.UserRepository
import com.bittokazi.watch.navigator.utils.Utils
import com.bittokazi.watch.navigator.validation.config.ValidationConfiguration
import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface ValidationService {
    fun count(id: Long): CallResult<MonthlyCountData, ValidityServiceError>

    fun isValid(id: Long): CallResult<Boolean, ValidityServiceError>
}

class DefaultValidationService(
    private val monthlyCountRepository: MonthlyCountRepository,
    private val userRepository: UserRepository,
    private val validationConfiguration: ValidationConfiguration,
) : ValidationService {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    init {
        log.info("[ValidationService] -> init")
    }

    override fun isValid(id: Long): CallResult<Boolean, ValidityServiceError> {
        return userRepository.findOneById(id)?.let { user ->
            val yearMonth = Utils.getCurrentYearMonth()
            monthlyCountRepository.getByUserIdAndYearMonth(
                user.id!!,
                yearMonth,
            )?.let { monthlyCount ->
                when {
                    monthlyCount.requestCount >= validationConfiguration.limit -> CallResult.Success(false)
                    else -> CallResult.Success(true)
                }
            } ?: CallResult.Success(true)
        } ?: CallResult.Failure(ValidityServiceError.UNPROCESSABLE_ENTITY)
    }

    override fun count(id: Long): CallResult<MonthlyCountData, ValidityServiceError> {
        val yearMonth = Utils.getCurrentYearMonth()

        return userRepository.findOneById(id)?.let { user ->
            CallResult.Success(
                monthlyCountRepository.getByUserIdAndYearMonth(
                    user.id!!,
                    yearMonth,
                )?.let { monthlyCount ->
                    monthlyCountRepository.update(
                        monthlyCount.apply {
                            requestCount += 1
                        },
                    )
                } ?: monthlyCountRepository.create(
                    MonthlyCountData(
                        user = user,
                        yearMonth = yearMonth,
                        requestCount = 1,
                    ),
                ),
            )
        } ?: CallResult.Failure(ValidityServiceError.UNPROCESSABLE_ENTITY)
    }
}

enum class ValidityServiceError {
    UNPROCESSABLE_ENTITY,
}
