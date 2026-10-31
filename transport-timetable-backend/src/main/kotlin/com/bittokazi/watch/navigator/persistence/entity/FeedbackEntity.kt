package com.bittokazi.watch.navigator.persistence.entity

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Serializable
data class FeedbackData(
    val id: Long? = null,
    val user: UserData,
    var content: String,
    var status: FeedbackStatus,
    var createdAt: String?,
    var updatedAt: String?,
)

@OptIn(ExperimentalTime::class)
class Feedback(id: EntityID<Long>) : LongEntity(id), DtoConverter<FeedbackData> {
    companion object : LongEntityClass<Feedback>(Feedbacks)

    var user by User referencedOn Feedbacks.user
    var content by Feedbacks.content
    var status by Feedbacks.status
    var createdAt by Feedbacks.createdAt
    var updatedAt by Feedbacks.updatedAt

    override fun toDto(): FeedbackData {
        return FeedbackData(
            id = id.value,
            user = user.toData(false),
            content = content,
            status = FeedbackStatus.valueOf(status),
            createdAt = createdAt?.toString(),
            updatedAt = updatedAt?.toString(),
        )
    }
}

@OptIn(ExperimentalTime::class)
object Feedbacks : LongIdTable("feedback") {
    val user = reference("user_id", Users)
    val content: Column<String> = varchar("content", length = 1000)
    val status: Column<String> = varchar("status", length = 255)
    val createdAt: Column<Instant?> = timestamp("created_at").nullable()
    val updatedAt: Column<Instant?> = timestamp("updated_at").nullable()
}

enum class FeedbackStatus {
    CREATED,
    RESOLVED,
}

