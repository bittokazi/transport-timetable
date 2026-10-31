package com.bittokazi.watch.navigator.security.entity

import com.bittokazi.watch.navigator.persistence.entity.RoleEnum
import com.bittokazi.watch.navigator.persistence.entity.ScopeEnum

object RoleScopeConfiguration {
    val roles =
        mapOf(
            RoleEnum.ROLE_SUPER_ADMIN to
                setOf(
                    ScopeEnum.ROLE_SUPER_ADMIN,
                    ScopeEnum.USER_WHO_AM_I,
                    ScopeEnum.USER_READ_ALL,
                    ScopeEnum.USER_READ,
                    ScopeEnum.USER_ADD,
                    ScopeEnum.USER_UPDATE,
                    ScopeEnum.ROLE_READ_ALL,
                    ScopeEnum.FEEDBACK_READ_ALL,
                    ScopeEnum.FEEDBACK_RESOLVE,
                ),
            RoleEnum.ROLE_USER to
                setOf(
                    ScopeEnum.ROLE_USER,
                    ScopeEnum.USER_WHO_AM_I,
                    ScopeEnum.USER_TRANSPORT_SEARCH,
                    ScopeEnum.USER_TRANSPORT_PLAN,
                    ScopeEnum.USER_TRANSPORT_MAP,
                    ScopeEnum.FEEDBACK_CREATE,
                ),
        )
}
