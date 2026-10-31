package com.bittokazi.watch.navigator.app.tenant.services

import com.bittokazi.watch.navigator.app.config.AppInfo
import com.bittokazi.watch.navigator.app.config.AppModuleConfiguration
import com.bittokazi.watch.navigator.persistence.entity.CallResult
import com.bittokazi.watch.navigator.validation.config.ValidationConfiguration
import io.ktor.server.application.ApplicationCall
import org.slf4j.LoggerFactory

class DefaultInfoService(
    private val appModuleConfiguration: AppModuleConfiguration,
    private val appInfo: AppInfo,
    private val validationConfiguration: ValidationConfiguration
) : InfoService {
    private val log = LoggerFactory.getLogger(javaClass)

    init {
        log.info("[TenantService] -> init")
    }

    override fun getTenantInfo(call: ApplicationCall): CallResult<SystemInfo, InfoServiceError> {
        return CallResult.Success(
            SystemInfo(
                true,
                enabledConfigPanel = true,
                name = appModuleConfiguration.appName,
                systemVersion = appInfo.version,
                changeLogs = appInfo.changeLogs,
                limit = validationConfiguration.limit,
                upgradeEnabled = appModuleConfiguration.enableUpgrade,
                loginEnabled = appModuleConfiguration.enableLogin,
                appVersion = "v${appInfo.appVersion.name}",
                buildVersion = appInfo.appVersion.buildVersion
            ),
        )
    }
}
