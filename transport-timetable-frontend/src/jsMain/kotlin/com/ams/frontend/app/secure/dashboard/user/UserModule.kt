package com.ams.frontend.app.secure.dashboard.user

import com.ams.frontend.app.secure.dashboard.user.components.UserAddComponent
import com.ams.frontend.app.secure.dashboard.user.components.UserListComponent
import com.ams.frontend.app.secure.dashboard.user.components.UserUpdateComponent
import com.ams.frontend.base.AppEngine
import com.bittokazi.kvision.spa.framework.base.common.RouterConfiguration
import com.bittokazi.kvision.spa.framework.base.common.module.DefaultSecuredPageModule

fun userModule() =
    DefaultSecuredPageModule(
        RouterConfiguration(
            route = AppEngine.APP_DASHBOARD_USER_ADD_ROUTE,
            title = "Add User",
            view = {
                UserAddComponent()
            },
        ),
        RouterConfiguration(
            route = AppEngine.APP_DASHBOARD_USER_ROUTE,
            title = "All Users",
            view = {
                UserListComponent()
            },
        ),
        RouterConfiguration(
            route = AppEngine.APP_DASHBOARD_USER_UPDATE_ROUTE(":id"),
            title = "Update User",
            view = {
                UserUpdateComponent(it.data.id)
            },
        ),
    )
