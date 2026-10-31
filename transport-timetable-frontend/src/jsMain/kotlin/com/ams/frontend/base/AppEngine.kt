package com.ams.frontend.base

import com.ams.frontend.base.config.MenuConfigurationProvider
import com.ams.frontend.base.models.User
import com.ams.frontend.base.services.AuthService
import com.ams.frontend.base.services.InfoService
import com.bittokazi.kvision.spa.framework.base.services.RestService
import io.kvision.state.ObservableValue

object AppEngine {
    lateinit var authService: AuthService
    lateinit var restService: RestService
    lateinit var infoService: InfoService
    lateinit var menuConfigurationProvider: MenuConfigurationProvider

    var userInfoChangeObserver: ObservableValue<User?> = ObservableValue(null)

    const val APP_BASE_ROUTE = "/app"
    const val APP_DASHBOARD_ROUTE = "$APP_BASE_ROUTE/dashboard"

    const val APP_DASHBOARD_USER_ROUTE = "$APP_DASHBOARD_ROUTE/users"
    const val APP_DASHBOARD_USER_ADD_ROUTE = "$APP_DASHBOARD_ROUTE/users/add"
    val APP_DASHBOARD_USER_UPDATE_ROUTE: (identifier: String) -> String = {
        "${APP_DASHBOARD_USER_ADD_ROUTE}/$it/update"
    }

    // Feedback
    const val APP_DASHBOARD_FEEDBACK_ROUTE = "$APP_DASHBOARD_ROUTE/feedback"
    const val APP_DASHBOARD_FEEDBACK_ADD_ROUTE = "$APP_DASHBOARD_FEEDBACK_ROUTE/add"
}
