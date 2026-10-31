package com.bittokazi.watch.navigator.persistence.repository

import com.bittokazi.watch.navigator.database.config.MultiTenantDatabaseConfiguration
import com.bittokazi.watch.navigator.persistence.entity.Feedback
import com.bittokazi.watch.navigator.persistence.entity.FeedbackData
import com.bittokazi.watch.navigator.persistence.entity.FeedbackStatus
import com.bittokazi.watch.navigator.persistence.entity.Feedbacks
import com.bittokazi.watch.navigator.persistence.entity.PaginatedResponse
import com.bittokazi.watch.navigator.persistence.entity.PaginationRequest
import com.bittokazi.watch.navigator.persistence.entity.User
import com.bittokazi.watch.navigator.persistence.entity.Users
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.lowerCase
import org.jetbrains.exposed.v1.jdbc.select
import kotlin.time.Clock

class FeedbackRepository(
    multiTenantDatabaseConfiguration: MultiTenantDatabaseConfiguration,
) : BaseRepository(multiTenantDatabaseConfiguration) {
    fun create(
        userId: Long,
        content: String,
    ): FeedbackData {
        return query {
            val user = User.findById(userId) ?: error("User not found")
            val feedback =
                Feedback.new {
                    this.user = user
                    this.content = content
                    status = FeedbackStatus.CREATED.name
                    createdAt = Clock.System.now()
                    updatedAt = Clock.System.now()
                }
            feedback.toDto()
        }
    }

    fun findById(id: Long): FeedbackData? {
        return query {
            Feedback.findById(id)?.toDto()
        }
    }

    fun findAll(paginationRequest: PaginationRequest): PaginatedResponse<FeedbackData> {
        return query {
            getPaginatedResponse(
                createPaginationQuery(
                    Feedback,
                    Feedbacks.innerJoin(Users).select(Feedbacks.columns),
                    paginationRequest,
                    false,
                ) { k, v ->
                    when (k) {
                        "status" -> Feedbacks.status.lowerCase() like "%${v.lowercase()}%"
                        "userId" -> Feedbacks.user eq v.toLong()
                        else -> null
                    }
                },
                paginationRequest.count?.toLong() ?: 10L,
                paginationRequest.page?.toLong() ?: 1L,
            ) {
                it.orderBy(Feedbacks.createdAt to SortOrder.DESC)
            }
        }
    }

    fun resolve(id: Long): FeedbackData? {
        return query {
            Feedback.findByIdAndUpdate(id) { feedback ->
                feedback.status = FeedbackStatus.RESOLVED.name
                feedback.updatedAt = Clock.System.now()
            }?.toDto()
        }
    }
}
