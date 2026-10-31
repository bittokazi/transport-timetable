package com.ams.frontend.base.models

import kotlinx.serialization.Serializable

@Serializable
data class Feedback(
    val id: Long? = null,
    val user: User = User(),
    var content: String = "",
    var status: FeedbackStatus = FeedbackStatus.CREATED,
    var createdAt: String? = null,
    var updatedAt: String? = null,
)

@Serializable
data class FeedbackCreateRequest(
    val content: String,
)

enum class FeedbackStatus {
    CREATED,
    RESOLVED,
}

