package com.bittokazi.watch.navigator.persistence.repository

import at.favre.lib.crypto.bcrypt.BCrypt
import com.bittokazi.watch.navigator.database.config.MultiTenantDatabaseConfiguration
import com.bittokazi.watch.navigator.persistence.entity.MonthlyCounts
import com.bittokazi.watch.navigator.persistence.entity.PaginatedResponse
import com.bittokazi.watch.navigator.persistence.entity.PaginationRequest
import com.bittokazi.watch.navigator.persistence.entity.Roles
import com.bittokazi.watch.navigator.persistence.entity.User
import com.bittokazi.watch.navigator.persistence.entity.UserData
import com.bittokazi.watch.navigator.persistence.entity.UserRoles
import com.bittokazi.watch.navigator.persistence.entity.Users
import com.bittokazi.watch.navigator.persistence.entity.Users.hashedPassword
import com.bittokazi.watch.navigator.utils.Utils
import korlibs.time.minutes
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.lowerCase
import org.jetbrains.exposed.v1.jdbc.andWhere
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.text.toLong
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class UserRepository(
    multiTenantDatabaseConfiguration: MultiTenantDatabaseConfiguration,
) : BaseRepository(multiTenantDatabaseConfiguration) {
    init {
        log.info("[UserRepository] -> created")
    }

    fun findOneByEmail(
        email: String,
        password: Boolean = false,
    ): UserData? {
        return query {
            User.find {
                Users.email eq email
            }.singleOrNull()?.toData(password) ?: run { null }
        }
    }

    fun findOneByEmail(
        tenant: String,
        email: String,
    ): UserData? {
        return query {
            User.find {
                Users.email eq email
            }.singleOrNull()?.toData() ?: run { null }
        }
    }

    fun findOneById(
        id: Long,
        password: Boolean = false,
    ): UserData? {
        return query {
            User.find {
                Users.id eq id
            }.singleOrNull()?.toData(password) ?: run { null }
        }
    }

    fun getAll(paginationRequest: PaginationRequest): PaginatedResponse<UserData> {
        log.info("[UserRepository] -> getAll() -> paginationRequest: $paginationRequest")
        return query {
            val currentYearMonth = Utils.getCurrentYearMonth()
            getPaginatedResponse(
                createPaginationQuery(
                    User,
                    Users
                        .innerJoin(UserRoles)
                        .innerJoin(Roles)
                        .join(
                            otherTable = MonthlyCounts,
                            joinType = JoinType.LEFT,
                            onColumn = Users.id,
                            otherColumn = MonthlyCounts.user,
                            additionalConstraint = {
                                MonthlyCounts.yearMonth eq currentYearMonth
                            },
                        )
                        .select(Users.columns + MonthlyCounts.requestCount),
                    paginationRequest,
                    true,
                ) { k, v ->
                    when (k) {
                        "email" -> Users.email.lowerCase() like "%${v.lowercase()}%"
                        "firstName" -> Users.firstName.lowerCase() like "%${v.lowercase()}%"
                        "lastName" -> Users.lastName.lowerCase() like "%${v.lowercase()}%"
                        "role" -> Roles.roleKey eq v
                        else -> null
                    }
                },
                paginationRequest.count?.toLong() ?: 1L,
                paginationRequest.page?.toLong() ?: 1L,
            ) {
                // Apply sorting inside the configuration block if your helper supports it
                it.orderBy(Users.id to SortOrder.DESC)
            }
        }
    }

    fun insert(userData: UserData): UserData {
        return query {
            val newUser =
                User.new {
                    email = userData.email
                    firstName = userData.firstName
                    lastName = userData.lastName
                    userData.password.let {
                        hashedPassword = BCrypt.withDefaults().hashToString(12, it.toCharArray())
                    }
                }.toData()

            if (newUser.id != null) {
                userData.roles?.let { roleDtos ->
                    roleDtos.forEach { roleDto ->
                        UserRoles.upsert {
                            it[userId] = newUser.id.let { it1 -> User.findById(it1)!!.id }
                            it[roleId] = roleDto.id!!
                        }
                    }
                }
            }
            newUser
        }
    }

    fun updateProfile(
        id: Long,
        userData: UserData,
    ): UserData? {
        return query {
            User.findByIdAndUpdate(id) { user ->
                user.firstName = userData.firstName
                user.lastName = userData.lastName
                user.email = userData.email
            }?.toData() ?: run { null }
        }
    }

    fun resetPassword(
        id: Long,
        password: String,
    ): UserData? {
        return query {
            User.findByIdAndUpdate(id) { user ->
                user.hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray())
            }?.toData()
        }
    }
}
