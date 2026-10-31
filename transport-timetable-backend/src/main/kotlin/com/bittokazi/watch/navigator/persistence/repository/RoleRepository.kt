package com.bittokazi.watch.navigator.persistence.repository

import com.bittokazi.watch.navigator.database.config.MultiTenantDatabaseConfiguration
import com.bittokazi.watch.navigator.persistence.entity.Role
import com.bittokazi.watch.navigator.persistence.entity.RoleDto
import com.bittokazi.watch.navigator.persistence.entity.RoleEnum
import com.bittokazi.watch.navigator.persistence.entity.Roles
import org.jetbrains.exposed.v1.core.eq
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class RoleRepository(
    multiTenantDatabaseConfiguration: MultiTenantDatabaseConfiguration,
) : BaseRepository(multiTenantDatabaseConfiguration) {
    init {
        log.info("[RoleRepository] -> created")
    }

    fun findOneByKey(roleKey: RoleEnum): RoleDto? {
        return query {
            Role.find {
                Roles.roleKey eq roleKey.toString()
            }.singleOrNull()?.toDto() ?: run { null }
        }
    }

    fun findAll(): List<RoleDto> {
        return query {
            Role.all().map { it.toDto() }
        }
    }
}
