package com.ams.frontend.app.open

import com.ams.frontend.app.secure.dashboard.user.components.form.UserForm
import com.ams.frontend.app.secure.dashboard.user.components.form.userFormErrorHandler
import com.ams.frontend.base.AppEngine
import com.ams.frontend.base.AppEngine.authService
import com.ams.frontend.base.models.User
import com.bittokazi.kvision.spa.framework.base.common.ObservableManager
import com.bittokazi.kvision.spa.framework.base.common.SpaAppEngine
import com.bittokazi.kvision.spa.framework.base.components.form.*
import com.bittokazi.kvision.spa.framework.base.services.SpaTenantService.tenantInfoObserver
import com.bittokazi.kvision.spa.framework.base.utils.sweetAlert
import io.kvision.core.FontWeight
import io.kvision.core.onClick
import io.kvision.core.onEvent
import io.kvision.form.form
import io.kvision.html.*
import io.kvision.panel.SimplePanel
import io.kvision.rest.RemoteRequestException
import io.kvision.state.ObservableValue
import io.kvision.state.bind
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition
import io.kvision.utils.perc
import io.kvision.utils.px
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToDynamic
import org.w3c.dom.events.Event

private const val COMPANY_INFO_OBSERVER_VERSION = "companyInfoObserverVersion"

class SignUpPage() : SimplePanel() {
    val signUpForm = SignUpForm()

    val errorMessage: ObservableValue<String> = ObservableValue("")

    val submitForm: ((Event) -> Unit) = { ev ->
        ev.preventDefault()
        if (errorMessage.value != "") {
            errorMessage.setState("")
        }

        if (!signUpForm.isValid()) {
            errorMessage.setState("Invalid Input")
            signUpForm.enforceValidation()
        } else {
            authService.register(
                User(
                    email = signUpForm.email.getValue(),
                    firstName = signUpForm.firstName.getValue(),
                    lastName = signUpForm.lastName.getValue(),
                    roles = listOf(),
                    password = signUpForm.password.getValue(),
                ),
            ).then {
                Toast.success(
                    "Signup Successful. Please Login now.",
                    ToastOptions(
                        position = ToastPosition.BOTTOMLEFT,
                        duration = 10000,
                        className = "kv-toastify-success toast-center-bottom",
                    ),
                )

                sweetAlert.fire(
                    Json.encodeToDynamic(
                        mapOf(
                            "title" to "Success",
                            "text" to "Signup Successful. Please Login now.",
                            "icon" to "success",
                        ),
                    ),
                )
                SpaAppEngine.routing.navigate("/app/login")
            }.catch { throwable ->

                if (throwable is RemoteRequestException) {
                    if (throwable.code.toInt() == 400) {
                        Toast.danger(
                            "Error Signup [${throwable.message}]",
                            ToastOptions(
                                position = ToastPosition.BOTTOMLEFT,
                                duration = 5000,
                                className = "kv-toastify-danger toast-center-bottom",
                            ),
                        )
                        throwable.response?.text()?.then {
                            val response: Map<String, String?> = Json.decodeFromString(it)
                            return@then signUpFormErrorHandler(response, signUpForm)
                        }
                    } else {
                        Toast.danger(
                            "Error Signup [${throwable.code.toInt()}]",
                            ToastOptions(
                                position = ToastPosition.BOTTOMLEFT,
                                duration = 5000,
                                className = "kv-toastify-danger toast-center-bottom",
                            ),
                        )
                    }
                } else {
                    Toast.danger(
                        "Error Signup",
                        ToastOptions(
                            position = ToastPosition.BOTTOMLEFT,
                            duration = 5000,
                            className = "kv-toastify-danger toast-center-bottom",
                        ),
                    )
                }
            }
        }
    }

    init {
        when (SpaAppEngine.defaultAuthHolder.getAuth()) {
            null ->
                main(className = "d-flex w-100") {
                    div(className = "container d-flex flex-column") {
                        div(className = "row vh-100") {
                            div(className = "col-sm-10 col-md-8 col-lg-6 col-xl-5 mx-auto d-table h-100") {
                                div(className = "d-table-cell align-middle") {
                                    div(className = "text-center mt-4") {
                                        h1(className = "h2").bind(tenantInfoObserver) {
                                            content = "Register"
                                        }
                                        p("Sign up to use the app", className = "lead")
                                    }

                                    div(className = "card") {
                                        div(className = "card-body") {
                                            div(className = "m-sm-3") {
                                                form {
                                                    width = 100.perc
                                                    div(className = "row") {
                                                        div(className = "col-md-12") {
                                                            add(textInputComponent(signUpForm.email, ""))
                                                        }
                                                        div(className = "col-md-12") {
                                                            add(textInputComponent(signUpForm.firstName, ""))
                                                        }
                                                        div(className = "col-md-12") {
                                                            add(textInputComponent(signUpForm.lastName, ""))
                                                        }
                                                        div(className = "col-md-12") {
                                                            add(textInputComponent(signUpForm.password, "", inputType = InputType.PASSWORD))
                                                        }

                                                        div(className = "col-md-12") {
                                                            add(
                                                                buttonComponent(
                                                                    signUpForm.submitButton,
                                                                    "Submit",
                                                                ).apply {
                                                                    width = 100.perc
                                                                    height = 48.px
                                                                    borderRadius = 12.px
                                                                    fontSize = 18.px
                                                                    fontWeight = FontWeight.BOLD
                                                                },
                                                            )
                                                        }
                                                    }

                                                    onEvent {
                                                        submit = submitForm
                                                    }
                                                }
                                            }
                                        }

                                        div(className = "text-center mb-3") {
                                            span("Want to login to your account?&nbsp;", rich = true)
                                            link("Continue here", "/app/login", dataNavigo = true)
                                        }
                                    }

                                    ObservableManager.setSubscriber(COMPANY_INFO_OBSERVER_VERSION) {
                                        tenantInfoObserver.subscribe {
                                            if (it != null) {
                                                add(VersionInfoBar())
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            else -> SpaAppEngine.routing.navigate("/app/dashboard")
        }
    }
}

class SignUpForm() : FormControl<Unit, Unit> {
    val email =
        FormTextInput(
            label = "Email",
            placeholder = "Enter your email address",
            defaultInvalidFeedback = "Missing email",
        ) {
            return@FormTextInput it?.matches(Regex("[^@]+@[^\\.]+\\..+")) ?: false
        }

    val firstName =
        FormTextInput(
            label = "First Name",
            placeholder = "Enter your first name",
            defaultInvalidFeedback = "Missing first name",
        ) {
            return@FormTextInput !it.isNullOrBlank()
        }

    val lastName =
        FormTextInput(
            label = "Last Name",
            placeholder = "Enter your last name",
            defaultInvalidFeedback = "Missing last name",
        ) {
            return@FormTextInput !it.isNullOrBlank()
        }

    val password =
        FormTextInput(
            label = "Password",
            placeholder = "",
            defaultInvalidFeedback = "Password must be 8+ chars with 1 uppercase, 1 lowercase, 1 number, and 1 special character",
        ) {
            val regex = Regex("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@\$%^&*-]).{8,}\$")
            return@FormTextInput it?.matches(regex) ?: false
        }

    val submitButton = FormButton()

    override fun setInput(input: Unit) {}

    override fun getInput() {}

    override fun isValid(): Boolean {
        return email.isValid() &&
            firstName.isValid() &&
            lastName.isValid() &&
            password.isValid()
    }

    override fun setCustomError(message: String) {}

    override fun enforceValidation() {
        firstName.enforceValidation()
        lastName.enforceValidation()
        email.enforceValidation()
        password.enforceValidation()
    }

    override fun getValue() {}

    fun initForm() {
    }
}

fun signUpFormErrorHandler(
    errors: Map<String, String?>?,
    signUpForm: SignUpForm,
) {
    if (errors != null) {
        if (errors["errorCode"]?.contains("EMAIL_EXIST") == true) {
            signUpForm.email.setCustomError("Email already exist")
        }
    }

    signUpForm.submitButton.resetInput()
}
