package com.bittokazi.watch.navigator.app.feedback.services

import com.bittokazi.watch.navigator.persistence.entity.CallResult
import com.bittokazi.watch.navigator.persistence.entity.FeedbackData
import com.bittokazi.watch.navigator.persistence.entity.PaginatedResponse
import com.bittokazi.watch.navigator.persistence.entity.PaginationRequest
import com.bittokazi.watch.navigator.persistence.repository.FeedbackRepository
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface FeedbackService {
    fun create(call: ApplicationCall, content: String): CallResult<FeedbackData, FeedbackServiceError>

    fun findAll(paginationRequest: PaginationRequest): PaginatedResponse<FeedbackData>

    fun resolve(call: ApplicationCall, id: Long): CallResult<FeedbackData, FeedbackServiceError>
}

class DefaultFeedbackService(
    private val feedbackRepository: FeedbackRepository,
) : FeedbackService {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    override fun create(
        call: ApplicationCall,
        content: String,
    ): CallResult<FeedbackData, FeedbackServiceError> {
        val trimmed = content.trim()
        if (trimmed.isEmpty() || trimmed.length > 1000) {
            return CallResult.Failure(FeedbackServiceError.UNPROCESSABLE_ENTITY)
        }

        val userId = call.principal<JWTPrincipal>()?.subject?.toLongOrNull()
            ?: return CallResult.Failure(FeedbackServiceError.NOT_FOUND)

        return CallResult.Success(feedbackRepository.create(userId, trimmed))
    }

    override fun findAll(paginationRequest: PaginationRequest): PaginatedResponse<FeedbackData> {
        return feedbackRepository.findAll(paginationRequest)
    }

    override fun resolve(
        call: ApplicationCall,
        id: Long,
    ): CallResult<FeedbackData, FeedbackServiceError> {
        return when (feedbackRepository.findById(id)) {
            null -> CallResult.Failure(FeedbackServiceError.NOT_FOUND)
            else -> {
                feedbackRepository.resolve(id)?.let { CallResult.Success(it) }
                    ?: CallResult.Failure(FeedbackServiceError.NOT_FOUND)
            }
        }
    }
}

enum class FeedbackServiceError {
    NOT_FOUND,
    UNPROCESSABLE_ENTITY,
}
