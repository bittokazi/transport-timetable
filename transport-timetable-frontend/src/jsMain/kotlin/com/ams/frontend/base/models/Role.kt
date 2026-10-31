package com.ams.frontend.base.models

import kotlinx.serialization.Serializable

@Serializable
data class Role(
    val id: Long? = null,
    val name: String = "",
    val roleKey: String = "",
)
