package com.bittokazi.watch.navigator.app.role.services

import com.bittokazi.watch.navigator.persistence.entity.RoleDto
import com.bittokazi.watch.navigator.persistence.repository.RoleRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface RoleService {
    fun findAll(): List<RoleDto>
}

class DefaultRoleService(
    private val roleRepository: RoleRepository,
) : RoleService {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    init {
        log.info("[RoleService] -> init")
    }

    override fun findAll(): List<RoleDto> {
        return roleRepository.findAll()
    }
}
