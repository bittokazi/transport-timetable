package com.bittokazi.watch.navigator.persistence.entity

import com.bittokazi.watch.navigator.utils.Utils
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Serializable
data class UserData(
    val id: Long? = null,
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    var password: String = "",
    val newPassword: String? = null,
    var roles: List<RoleDto>?,
    val image: String? = "https://www.gravatar.com/avatar/${Utils.md5(email)}?d=identicon",
)

@OptIn(ExperimentalTime::class)
class User(id: EntityID<Long>) : LongEntity(id), DtoConverter<UserData> {
    companion object : LongEntityClass<User>(Users)

    var firstName by Users.firstName
    var lastName by Users.lastName
    var email by Users.email
    var password: String? = ""
    var hashedPassword by Users.hashedPassword
    var roles by Role.via(UserRoles)

    fun toData(
        addPassword: Boolean = false,
    ): UserData {
        return UserData(
            id.value, firstName, lastName, email, if (addPassword) hashedPassword else "", "",
            roles?.let { iterable ->
                iterable.toList().map {
                    it.toDto()
                }
            },
        )
    }

    override fun toDto(): UserData {
        return toData()
    }
}

@OptIn(ExperimentalTime::class)
object Users : LongIdTable("user") {
    val firstName: Column<String> = varchar("first_name", length = 255)
    val lastName: Column<String> = varchar("last_name", length = 255)
    val email: Column<String> = varchar("email", length = 255)
    val hashedPassword: Column<String> = varchar("password", length = 255)
}

object UserRoles : Table("user_role") {
    val userId = reference("user_id", Users.id)
    val roleId = reference("role_id", Roles.id)
    override val primaryKey = PrimaryKey(userId, roleId)
}
