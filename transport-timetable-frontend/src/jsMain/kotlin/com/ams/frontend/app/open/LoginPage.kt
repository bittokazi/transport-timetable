package com.ams.frontend.app.open

import com.ams.frontend.base.AppEngine
import com.bittokazi.kvision.spa.framework.base.common.AuthData
import com.bittokazi.kvision.spa.framework.base.common.ObservableManager
import com.bittokazi.kvision.spa.framework.base.common.SpaAppEngine
import com.bittokazi.kvision.spa.framework.base.services.SpaTenantService.tenantInfoObserver
import com.bittokazi.kvision.spa.framework.base.utils.sweetAlert
import io.kvision.core.UNIT
import io.kvision.core.getElementJQuery
import io.kvision.core.onClick
import io.kvision.core.onEvent
import io.kvision.form.form
import io.kvision.form.text.TextInput
import io.kvision.html.*
import io.kvision.panel.SimplePanel
import io.kvision.rest.*
import io.kvision.state.ObservableValue
import io.kvision.state.bind
import kotlinx.browser.window
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToDynamic
import org.w3c.dom.events.Event
import kotlin.js.Promise

private const val COMPANY_INFO_OBSERVER_LOGIN_PAGE = "companyInfoObserverLoginPage"
private const val COMPANY_INFO_OBSERVER_RESET_PASSWORD_LOGIN_PAGE = "companyInfoObserverResetPasswordLoginPage"
private const val COMPANY_INFO_OBSERVER_VERSION = "companyInfoObserverVersion"

class LoginPage() : SimplePanel() {
    private val routing = SpaAppEngine.routing
    private val authHolder = SpaAppEngine.defaultAuthHolder
    private val errorMessage: ObservableValue<String> = ObservableValue("")

    init {
        lateinit var email: TextInput
        lateinit var password: TextInput
        var tenantName = "Zepp Transport Timetable"

        var errorDiv: Div? = null

        val submitFunc: ((Event) -> Unit) = { ev ->
            ev.preventDefault()
            errorDiv?.content?.let {
                errorMessage.setState("")
            }
            if (email.value == null || password.value == null) {
                errorMessage.setState("Blank login details")
            } else {
                login(LoginRequest(email.value!!, password.value!!))
                    .then {
                        authHolder.setAuth(AuthData(it.token, it.refreshToken))
                        routing.navigate("/app/dashboard")
                    }.catch {
                        if (it is RemoteRequestException) {
                            console.log("e -> $it")
                            if (it.code.toInt() == 401) {
                                errorMessage.setState("Email or password do not match")
                            }
                        }
                    }
            }
        }

        errorMessage.subscribe {
            if (errorDiv != null && errorDiv?.visible == false) {
                errorDiv?.show()
                errorDiv?.getElementJQuery()?.hide(0)
            }
            errorDiv?.getElementJQuery()?.slideToggle(300).also { _it ->
                when (it) {
                    "" -> errorDiv?.getElementJQuery()?.fadeOut(300, "linear")
                    else -> {
                        errorDiv?.getElementJQuery()?.fadeIn(300, "linear")
                    }
                }
            }
        }

        main(className = "d-flex w-100") {
            div(className = "container d-flex flex-column") {
                div(className = "row vh-100") {
                    div(className = "col-sm-10 col-md-8 col-lg-6 col-xl-5 mx-auto d-table h-100") {
                        div(className = "d-table-cell align-middle") {
                            div(className = "text-center mt-4") {
                                h1(className = "h2").bind(tenantInfoObserver) {
                                    if (it != null && !it.cpanel) {
                                        tenantName = it.name ?: run { "" }
                                    }
                                    content = "Login to $tenantName"
                                }
                                p("Sign in to your account to continue", className = "lead")
                            }

                            div(className = "card") {
                                div(className = "card-body") {
                                    div(className = "m-sm-3") {
                                        form {
                                            ObservableManager.setSubscriber("companyInfoObserverLoginPage") {
                                                tenantInfoObserver.subscribe {
                                                    if (it != null) {
                                                        div(className = "d-grid gap-2 mt-3") {
                                                            button(
                                                                "Login",
                                                                type = ButtonType.BUTTON,
                                                                className = "btn btn-lg btn-danger",
                                                            ).onClick {
                                                                window.location.href = "/login"
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }.onEvent {
                                            submit = submitFunc
                                        }
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
                                        errorDiv?.hide()
                                    }
                                }
                                div(className = "text-center gap-2 mb-3") {
                                    span("Forgot your password?&nbsp;", rich = true)
                                    link("Reset here", "/app/reset-password", dataNavigo = true).toString()
                                }
                                div(className = "text-center mb-3") {
                                    span("Don't have an account?&nbsp;", rich = true)
                                    link("Sign up", "/app/signup", dataNavigo = true).toString()
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
    }
}

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class LoginResponse(
    val token: String,
    val refreshToken: String,
)

fun login(loginRequest: LoginRequest): Promise<LoginResponse> {
    val restClient = RestClient()
    return restClient.post<LoginResponse, LoginRequest>(AppEngine.restService.BASE_URL + "/login", loginRequest)
}
