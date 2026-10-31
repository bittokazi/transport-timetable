package com.bittokazi.watch.navigator.persistence.repository

import com.bittokazi.watch.navigator.database.config.MultiTenantDatabaseConfiguration
import com.bittokazi.watch.navigator.persistence.entity.MonthlyCount
import com.bittokazi.watch.navigator.persistence.entity.MonthlyCountData
import com.bittokazi.watch.navigator.persistence.entity.MonthlyCounts
import com.bittokazi.watch.navigator.persistence.entity.User
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MonthlyCountRepository(
    multiTenantDatabaseConfiguration: MultiTenantDatabaseConfiguration,
) : BaseRepository(multiTenantDatabaseConfiguration) {
    init {
        log.info("[MonthlyCountRepository] -> created")
    }

    /**
     * Finds a single MonthlyCount record matching both the userId and the target yearMonth.
     */
    fun getByUserIdAndYearMonth(
        userId: Long,
        yearMonth: String,
    ): MonthlyCountData? {
        return query {
            MonthlyCount.find {
                (MonthlyCounts.user eq userId) and (MonthlyCounts.yearMonth eq yearMonth)
            }.singleOrNull()?.toDto()
        }
    }

    /**
     * Standard insert method to create a new monthly tally record.
     */
    fun create(monthlyCountData: MonthlyCountData): MonthlyCountData {
        return query {
            MonthlyCount.find {
                (MonthlyCounts.user eq monthlyCountData.user.id) and (MonthlyCounts.yearMonth eq monthlyCountData.yearMonth)
            }.singleOrNull()?.toDto() ?: run {
                MonthlyCount.new {
                    user = User.findById(monthlyCountData.user.id!!)!!
                    yearMonth = monthlyCountData.yearMonth
                    requestCount = 1
                }.toDto()
            }
        }
    }

    /**
     * Utility method to increment or update an existing request tally.
     */
    fun update(monthlyCountData: MonthlyCountData): MonthlyCountData? {
        return query {
            MonthlyCount.findByIdAndUpdate(monthlyCountData.id!!) { record ->
                record.user = User.findById(monthlyCountData.user.id!!)!!
                record.yearMonth = monthlyCountData.yearMonth
                record.requestCount = monthlyCountData.requestCount
            }?.toDto()
        }
    }
}
