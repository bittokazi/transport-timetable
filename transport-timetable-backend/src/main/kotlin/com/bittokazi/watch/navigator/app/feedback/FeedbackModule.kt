package com.bittokazi.watch.navigator.app.feedback

import com.bittokazi.watch.navigator.app.feedback.controllers.feedbackRoute
import com.bittokazi.watch.navigator.app.feedback.services.DefaultFeedbackService
import com.bittokazi.watch.navigator.app.feedback.services.FeedbackService
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies

fun Application.feedbackModule() {
    dependencies {
        provide<FeedbackService>(DefaultFeedbackService::class)
    }

    feedbackRoute()
}

