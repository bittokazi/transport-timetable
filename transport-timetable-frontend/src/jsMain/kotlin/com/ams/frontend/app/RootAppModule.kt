package com.ams.frontend.app

import com.ams.frontend.app.open.HomePage
import com.ams.frontend.app.open.LoginPage
import com.ams.frontend.app.open.ResetPasswordPage
import com.ams.frontend.app.open.ResetPasswordRequestPage
import com.ams.frontend.app.open.SignUpPage
import com.ams.frontend.app.secure.dashboardModule
import com.ams.frontend.base.AppEngine
import com.bittokazi.kvision.spa.framework.base.common.RouterConfiguration
import com.bittokazi.kvision.spa.framework.base.common.module.ApplicationModule
import com.bittokazi.kvision.spa.framework.base.common.module.DefaultRootApplicationModule

fun rootAppModule(): ApplicationModule =
    DefaultRootApplicationModule(
        loginPage = {
            LoginPage()
        },
        securedModule = dashboardModule(),
        authInformationProvider = AppEngine.authService,
        RouterConfiguration(
            route = "/app",
            title = "Zepp Transport Timetable",
            view = {
                HomePage()
            },
        ),
        RouterConfiguration(
            route = "/app/signup",
            title = "Zepp Transport Timetable| Signup",
            view = {
                SignUpPage()
            },
        ),
        RouterConfiguration(
            route = "/app/reset-password",
            title = "Zepp Transport Timetable | Request Reset Password",
            view = {
                ResetPasswordRequestPage()
            },
        ),
        RouterConfiguration(
            route = "/app/reset-password/:token",
            title = "Zepp Transport Timetable | Reset Password",
            view = {
                ResetPasswordPage(it.data.token)
            },
        ),
    )
