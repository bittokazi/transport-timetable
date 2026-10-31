package com.ams.frontend.app.secure.dashboard.feedback

import com.ams.frontend.app.secure.dashboard.feedback.components.FeedbackAddComponent
import com.ams.frontend.app.secure.dashboard.feedback.components.FeedbackListComponent
import com.ams.frontend.base.AppEngine
import com.bittokazi.kvision.spa.framework.base.common.RouterConfiguration
import com.bittokazi.kvision.spa.framework.base.common.module.DefaultSecuredPageModule

fun feedbackModule() =
    DefaultSecuredPageModule(
        RouterConfiguration(
            route = AppEngine.APP_DASHBOARD_FEEDBACK_ROUTE,
            title = "Feedback",
            view = {
                FeedbackListComponent()
            },
        ),
        RouterConfiguration(
            route = AppEngine.APP_DASHBOARD_FEEDBACK_ADD_ROUTE,
            title = "Add Feedback",
            view = {
                FeedbackAddComponent()
            },
        ),
    )
