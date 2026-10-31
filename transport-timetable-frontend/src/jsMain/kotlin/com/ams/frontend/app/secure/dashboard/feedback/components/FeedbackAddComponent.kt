package com.ams.frontend.app.secure.dashboard.feedback.components

import com.ams.frontend.app.secure.dashboard.feedback.FeedbackService
import com.ams.frontend.app.secure.dashboard.feedback.components.form.FeedbackForm
import com.ams.frontend.app.secure.dashboard.feedback.components.form.feedbackFormComponent
import com.ams.frontend.base.AppEngine.APP_DASHBOARD_FEEDBACK_ROUTE
import com.ams.frontend.base.AppEngine.APP_DASHBOARD_ROUTE
import com.bittokazi.kvision.spa.framework.base.common.SpaAppEngine
import com.bittokazi.kvision.spa.framework.base.utils.sweetAlert
import io.kvision.html.div
import io.kvision.panel.SimplePanel
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToDynamic

class FeedbackAddComponent : SimplePanel() {
    private val feedbackForm = FeedbackForm()

    init {
        div {
            feedbackFormComponent(
                feedbackForm = feedbackForm,
            ) {
                when (feedbackForm.isValid()) {
                    true -> {
                        FeedbackService.create(
                            com.ams.frontend.base.models.Feedback(
                                content = feedbackForm.content.getValue().trim(),
                            ),
                        ).then {
                            sweetAlert.fire(
                                Json.encodeToDynamic(
                                    mapOf(
                                        "title" to "Success",
                                        "text" to "Feedback submitted successfully.",
                                        "icon" to "success",
                                    ),
                                ),
                            )
                            SpaAppEngine.routing.navigate(APP_DASHBOARD_ROUTE)
                        }.catch {
                            sweetAlert.fire(
                                Json.encodeToDynamic(
                                    mapOf(
                                        "title" to "Error",
                                        "text" to "Unable to submit feedback.",
                                        "icon" to "error",
                                    ),
                                ),
                            )
                        }
                    }
                    false -> {
                    }
                }
            }
        }
    }
}
