package com.ams.frontend.base.config

import com.ams.frontend.base.AppEngine
import com.ams.frontend.base.models.RoleEnum
import com.bittokazi.kvision.spa.framework.base.common.SpaAppEngine
import com.bittokazi.kvision.spa.framework.base.components.menu.MenuItem
import com.bittokazi.kvision.spa.framework.base.components.menu.MenuProvider
import com.bittokazi.kvision.spa.framework.base.components.menu.MenuSection
import com.bittokazi.kvision.spa.framework.base.models.SpaRole

class MenuConfigurationProvider : MenuProvider {
    override fun getMenu(spaRole: SpaRole): List<MenuSection> {
        val menuItems: MutableList<MenuItem> = mutableListOf()

        when (RoleEnum.valueOf(spaRole.name!!)) {
            RoleEnum.ROLE_SUPER_ADMIN -> {
                menuItems.add(MenuItem("Dashboard", AppEngine.APP_DASHBOARD_ROUTE, "home", listOf()))
                menuItems.add(
                    MenuItem(
                        "Users",
                        "",
                        "users",
                        listOf(
                            MenuItem("All Users", AppEngine.APP_DASHBOARD_USER_ROUTE, ""),
                            MenuItem("Add Users", AppEngine.APP_DASHBOARD_USER_ADD_ROUTE, ""),
                        ),
                    ),
                )
                menuItems.add(
                    MenuItem(
                        "Feedback",
                        "",
                        "message-square",
                        listOf(
                            MenuItem("All Feedback", AppEngine.APP_DASHBOARD_FEEDBACK_ROUTE, ""),
                        ),
                    ),
                )
                menuItems.add(
                    MenuItem(
                        "",
                        AppEngine.APP_DASHBOARD_USER_UPDATE_ROUTE(":id"),
                        "",
                        accessCheckOnly = true
                    ),
                )
            }

            RoleEnum.ROLE_USER -> {
                menuItems.add(MenuItem("Dashboard", AppEngine.APP_DASHBOARD_ROUTE, "home", listOf()))
                menuItems.add(MenuItem("My Profile", SpaAppEngine.APP_DASHBOARD_ACCOUNT_SETTINGS_ROUTE, "user", listOf()))
                menuItems.add(MenuItem("Send Feedback", AppEngine.APP_DASHBOARD_FEEDBACK_ADD_ROUTE, "message-square"))
            }
        }

        return listOf(
            MenuSection(
                title =
                    when (RoleEnum.valueOf(spaRole.name!!)) {
                        RoleEnum.ROLE_SUPER_ADMIN -> "Management Tools"

                        RoleEnum.ROLE_USER -> "Account Management"
                    },
                link = "",
                icon = "",
                menuItems = menuItems,
            ),
            MenuSection(
                title = "System",
                link = "",
                icon = "",
                menuItems = listOf(
                    MenuItem(
                        "Status Page",
                        "https://stats.uptimerobot.com/v5qGxtPGOI",
                        "activity",
                        listOf(),
                        true
                    )
                ),
            ),
        )
    }
}
