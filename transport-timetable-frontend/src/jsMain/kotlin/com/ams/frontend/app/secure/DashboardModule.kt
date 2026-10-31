package com.ams.frontend.app.secure

import com.ams.frontend.app.secure.dashboard.DashboardHomePage
import com.ams.frontend.app.secure.dashboard.account.accountModule
import com.ams.frontend.app.secure.dashboard.user.userModule
import com.ams.frontend.app.secure.dashboard.feedback.feedbackModule
import com.ams.frontend.base.AppEngine
import com.ams.frontend.base.layout.CustomDashboardLayoutLoader
import com.bittokazi.kvision.spa.framework.base.common.RouterConfiguration
import com.bittokazi.kvision.spa.framework.base.common.module.DefaultSecuredModule
import com.bittokazi.kvision.spa.framework.base.layouts.dashboard.layout.ContentContainerType

fun dashboardModule() =
    DefaultSecuredModule(
        layoutLoader = CustomDashboardLayoutLoader(),
        modules =
            listOf(
                userModule(),
                feedbackModule(),
                accountModule(),
            ),
        RouterConfiguration(
            route = AppEngine.APP_DASHBOARD_ROUTE,
            title = "Dashboard | Zepp Transport Timetable",
            view = {
                DashboardHomePage()
            },
            dashboardContainer = ContentContainerType.NO_CARD,
        ),
    )
