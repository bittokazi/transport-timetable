package com.ams.frontend.app.secure.dashboard.feedback.components.form

import com.bittokazi.kvision.spa.framework.base.components.form.FormButton
import com.bittokazi.kvision.spa.framework.base.components.form.FormControl
import com.bittokazi.kvision.spa.framework.base.components.form.FormTextInput
import com.bittokazi.kvision.spa.framework.base.components.form.buttonComponent
import com.bittokazi.kvision.spa.framework.base.components.form.textInputComponent
import io.kvision.core.Container
import io.kvision.core.UNIT
import io.kvision.core.getElementJQuery
import io.kvision.core.onEvent
import io.kvision.form.form
import io.kvision.html.Div
import io.kvision.html.div
import io.kvision.state.ObservableValue
import kotlinx.browser.window
import org.w3c.dom.events.Event

fun Container.feedbackFormComponent(
    feedbackForm: FeedbackForm,
    submitCallback: () -> Unit,
): Container {
    var errorDiv: Div? = null
    val errorMessage = ObservableValue("")

    val submitForm: (Event) -> Unit = { ev ->
        ev.preventDefault()
        if (errorMessage.value != "") {
            errorMessage.setState("")
        }

        if (!feedbackForm.isValid()) {
            errorMessage.setState("Invalid Input")
            feedbackForm.enforceValidation()
        } else {
            feedbackForm.submitButton.showLoading()
            submitCallback()
        }
    }

    errorMessage.subscribe {
        errorDiv?.getElementJQuery()?.slideToggle()
        when (it) {
            "" -> errorDiv?.getElementJQuery()?.fadeOut(300, "linear")
            else -> errorDiv?.getElementJQuery()?.fadeIn(300, "linear")
        }
    }

    return form {
        div(className = "row mb-3") {
            div(className = "col-md-12") {
                add(textInputComponent(feedbackForm.content, ""))
            }
            div(className = "col-md-12 mt-3") {
                add(buttonComponent(feedbackForm.submitButton, "Submit Feedback"))
            }
            div(className = "col-md-12") {
                window.setTimeout({
                    if (errorDiv == null) {
                        errorDiv =
                            Div(className = "alert alert-danger align-items-center") {
                                marginTop = 10 to UNIT.px
                                setAttribute("role", "alert")
                                div {
                                    errorMessage.subscribe {
                                        content = it
                                    }
                                }
                            }
                        add(errorDiv!!)
                        errorDiv?.getElementJQuery()?.hide(0)
                    }
                }, 50)
            }
        }
        onEvent {
            submit = submitForm
        }
        window.setTimeout({
            feedbackForm.initForm()
        }, 100)
    }
}

class FeedbackForm : FormControl<Unit, Unit> {
    val content =
        FormTextInput(
            label = "Feedback",
            placeholder = "Tell us what you want to share",
            defaultInvalidFeedback = "Feedback is required and must be at most 1000 characters long",
        ) {
            val trimmed = it?.trim().orEmpty()
            trimmed.isNotBlank() && trimmed.length <= 1000
        }

    val submitButton = FormButton()

    override fun setInput(input: Unit) {}

    override fun getInput() {}

    override fun isValid(): Boolean = content.isValid()

    override fun setCustomError(message: String) {}

    override fun enforceValidation() {
        content.enforceValidation()
    }

    override fun getValue() {}

    fun initForm() {
        if (content.isValid()) content.enforceValidation()
    }
}

