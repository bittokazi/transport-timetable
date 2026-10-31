package com.ams.frontend

import com.ams.frontend.app.rootAppModule
import com.ams.frontend.base.AppEngine
import com.ams.frontend.base.config.MenuConfigurationProvider
import com.ams.frontend.base.services.AuthService
import com.ams.frontend.base.services.InfoService
import com.bittokazi.kvision.spa.framework.base.common.ApplicationConfiguration
import com.bittokazi.kvision.spa.framework.base.common.AuthHolderType
import com.bittokazi.kvision.spa.framework.base.common.SpaAppEngine
import com.bittokazi.kvision.spa.framework.base.common.SpaApplication
import com.bittokazi.kvision.spa.framework.base.models.SpaTenantInfo
import com.bittokazi.kvision.spa.framework.base.utils.importDefaultResources

fun main() {
    importDefaultResources()
    SpaApplication.init()
    SpaAppEngine.restService.REFRESH_TOKEN_ENDPOINT = "${SpaAppEngine.restService.BASE_URL}/oauth/refresh/token"

    AppEngine.restService = SpaAppEngine.restService
    AppEngine.authService = AuthService()
    AppEngine.menuConfigurationProvider = MenuConfigurationProvider()
    AppEngine.infoService = InfoService()

    SpaAppEngine.APP_BASE_ROUTE = "/app"
    SpaAppEngine.APP_DASHBOARD_ROUTE = "/app/dashboard"
    SpaAppEngine.APP_LOGIN_ROUTE = "/app/login"
    SpaAppEngine.PAGE_ACCESS_CHECK = true

    SpaApplication.applicationConfiguration =
        ApplicationConfiguration(
            spaTenantInfo =
                SpaTenantInfo(
                    cpanel = true,
                    enabledConfigPanel = true,
                    name = "Zepp Transport Timetable",
                    systemVersion = "v9.9.9",
                ),
            isTenantEnabled = true,
            rootApplicationModule = rootAppModule(),
            tenantInformationProvider = AppEngine.infoService,
            authHolderType = AuthHolderType.COOKIE,
            menuProvider = AppEngine.menuConfigurationProvider,
            refreshTokenRequestProvider = AppEngine.authService,
            logoutActionProvider = AppEngine.authService,
        )

    SpaApplication.start()
}
