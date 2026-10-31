package com.ams.frontend.base.services

import com.ams.frontend.base.AppEngine.restService
import com.bittokazi.kvision.spa.framework.base.common.tenant.TenantInformationProvider
import com.bittokazi.kvision.spa.framework.base.models.SpaTenantInfo
import io.kvision.rest.HttpMethod
import io.kvision.rest.RestResponse
import io.kvision.rest.request
import kotlinx.serialization.Serializable
import kotlin.js.Promise

class InfoService : TenantInformationProvider {
    var changeLogs: List<String> = mutableListOf()
    var systemInfo: SystemInfo? = null


    fun getCompanyInfo(): Promise<RestResponse<SystemInfo>> {
        return restService.getPublicClient().request<SystemInfo>("${restService.BASE_URL}/api/v1/info") {
            method = HttpMethod.GET
        }
    }

    override fun getTenantInfoProvider(): Promise<SpaTenantInfo> {
        return Promise { resolve, reject ->
            getCompanyInfo().then {
                changeLogs = it.data.changeLogs
                systemInfo = it.data
                resolve(
                    SpaTenantInfo(
                        cpanel = it.data.cpanel,
                        enabledConfigPanel = it.data.enabledConfigPanel,
                        name = it.data.name,
                        systemVersion = it.data.systemVersion,
                    ),
                )
            }.catch { throwable ->
                console.log(throwable)
                reject(throwable)
            }
        }
    }

    fun getChangeLogs(): String {
        if(changeLogs.isEmpty()) {
            return "<p>No change logs available</p>"
        }
        return changeLogs.map { "<p>$it</p>" }
            .joinToString("")
    }
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
