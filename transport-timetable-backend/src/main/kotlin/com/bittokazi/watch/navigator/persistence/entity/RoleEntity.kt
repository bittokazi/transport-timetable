package com.bittokazi.watch.navigator.persistence.entity

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

@Serializable
data class RoleDto(
    val id: Long? = null,
    val name: String = "",
    val roleKey: String = "",
)

class Role(id: EntityID<Long>) : LongEntity(id), DtoConverter<RoleDto> {
    companion object : LongEntityClass<Role>(Roles)

    var name by Roles.name
    var roleKey by Roles.roleKey

    override fun toDto(): RoleDto {
        return RoleDto(id.value, name, roleKey)
    }
}

object Roles : LongIdTable("role") {
    val name: Column<String> = varchar("name", length = 255)
    val roleKey: Column<String> = varchar("role_key", length = 255)
}
