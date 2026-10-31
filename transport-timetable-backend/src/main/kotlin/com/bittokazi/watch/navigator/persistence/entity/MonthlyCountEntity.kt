package com.bittokazi.watch.navigator.persistence.entity

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

@Serializable
data class MonthlyCountData(
    val id: Long? = null,
    val user: UserData,
    var yearMonth: String,
    var requestCount: Long,
)

class MonthlyCount(id: EntityID<Long>) : LongEntity(id), DtoConverter<MonthlyCountData> {
    companion object : LongEntityClass<MonthlyCount>(MonthlyCounts)

    var user by User referencedOn MonthlyCounts.user
    var yearMonth by MonthlyCounts.yearMonth
    var requestCount by MonthlyCounts.requestCount

    override fun toDto(): MonthlyCountData {
        return MonthlyCountData(
            id.value,
            user.toData(false),
            yearMonth,
            requestCount,
        )
    }
}

object MonthlyCounts : LongIdTable("monthly_count") {
    // Maps to user_id int8 NOT NULL and references your existing Users table
    val user = reference("user_id", Users)

    // Maps to year_month varchar(255) NOT NULL
    val yearMonth: Column<String> = varchar("year_month", length = 255)

    // Maps to request_count int8 NOT NULL
    val requestCount: Column<Long> = long("request_count")
}
