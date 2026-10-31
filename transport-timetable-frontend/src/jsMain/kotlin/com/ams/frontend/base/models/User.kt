package com.ams.frontend.base.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Long? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val password: String? = null,
    var newPassword: String? = null,
    val roles: List<Role> = listOf(),
    val image: String? = null,
)
