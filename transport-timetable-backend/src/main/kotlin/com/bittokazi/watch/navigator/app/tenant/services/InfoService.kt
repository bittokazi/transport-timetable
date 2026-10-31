package com.bittokazi.watch.navigator.app.tenant.services

import com.bittokazi.watch.navigator.persistence.entity.CallResult
import io.ktor.server.application.ApplicationCall
import kotlinx.serialization.Serializable

interface InfoService {
    fun getTenantInfo(call: ApplicationCall): CallResult<SystemInfo, InfoServiceError>
}

@Serializable
data class SystemInfo(
    val cpanel: Boolean,
    val enabledConfigPanel: Boolean,
    val name: String,
    val systemVersion: String = "v0.0.0_Dev",
    val changeLogs: List<String> = listOf(),
    val limit: Long = 0,
    val upgradeEnabled: Boolean = true,
    val loginEnabled: Boolean = true,
    val appVersion: String = "v0.0.0_Dev",
    val buildVersion: String = "app-v0.0.0_Dev",
)

enum class InfoServiceError {
    NOT_FOUND,
    UNPROCESSABLE_ENTITY,
}
