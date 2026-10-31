package com.ams.frontend.app.secure.dashboard.account

import com.ams.frontend.app.secure.dashboard.account.components.MyProfileComponent
import com.ams.frontend.app.secure.dashboard.account.components.SecuritySettingsComponent
import com.bittokazi.kvision.spa.framework.base.common.RouterConfiguration
import com.bittokazi.kvision.spa.framework.base.common.SpaAppEngine.APP_DASHBOARD_ACCOUNT_SECURITY_ROUTE
import com.bittokazi.kvision.spa.framework.base.common.SpaAppEngine.APP_DASHBOARD_ACCOUNT_SETTINGS_ROUTE
import com.bittokazi.kvision.spa.framework.base.common.module.DefaultSecuredPageModule
import com.bittokazi.kvision.spa.framework.base.layouts.dashboard.layout.ContentContainerType

fun accountModule() =
    DefaultSecuredPageModule(
        RouterConfiguration(
            route = APP_DASHBOARD_ACCOUNT_SETTINGS_ROUTE,
            title = "Account Settings",
            view = {
                MyProfileComponent()
            },
            dashboardContainer = ContentContainerType.NO_CARD,
        ),
        RouterConfiguration(
            route = APP_DASHBOARD_ACCOUNT_SECURITY_ROUTE,
            title = "Security Settings",
            view = {
                SecuritySettingsComponent()
            },
            dashboardContainer = ContentContainerType.NO_CARD,
        ),
    )
