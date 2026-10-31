package com.ams.frontend.app.secure.dashboard.account.components

import com.ams.frontend.app.secure.dashboard.user.UserService
import com.ams.frontend.app.secure.dashboard.user.components.form.UserPasswordUpdateForm
import com.ams.frontend.app.secure.dashboard.user.components.form.userPasswordFormComponent
import com.ams.frontend.app.secure.dashboard.user.components.form.userPasswordFormErrorHandler
import com.ams.frontend.base.AppEngine
import com.ams.frontend.base.models.User
import com.bittokazi.kvision.spa.framework.base.common.ObservableManager
import com.bittokazi.kvision.spa.framework.base.common.SpaAppEngine
import com.bittokazi.kvision.spa.framework.base.common.SpaAppEngine.APP_DASHBOARD_ACCOUNT_SECURITY_ROUTE
import com.bittokazi.kvision.spa.framework.base.common.SpaAppEngine.APP_DASHBOARD_ACCOUNT_SETTINGS_ROUTE
import com.bittokazi.kvision.spa.framework.base.utils.sweetAlert
import io.kvision.html.*
import io.kvision.panel.SimplePanel
import io.kvision.rest.RemoteRequestException
import io.kvision.state.ObservableValue
import kotlinx.browser.window
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToDynamic
import org.w3c.dom.get

@OptIn(ExperimentalSerializationApi::class)
class SecuritySettingsComponent : SimplePanel() {
    val userPasswordUpdateForm = UserPasswordUpdateForm(self = true)
    lateinit var user: User
    val pageRefreshListner = ObservableValue(false)

    fun contentBody(): Div {
        return div {
            div(className = "row mt-3") {
                nav(className = "nav nav-pills nav-justified") {
                    link(
                        label = "",
                        url = APP_DASHBOARD_ACCOUNT_SETTINGS_ROUTE,
                        className = "nav-link",
                        dataNavigo = true,
                    ) {
                        span(className = "feather-sm me-1") {
                            setAttribute("data-feather", "user")
                        }
                        +" My Profile"
                    }
                    link(
                        label = "",
                        url = APP_DASHBOARD_ACCOUNT_SECURITY_ROUTE,
                        className = "nav-link active",
                        dataNavigo = true,
                    ) {
                        span(className = "feather-sm me-1") {
                            setAttribute("data-feather", "lock")
                        }
                        +" Security"
                    }
                }
            }
            div(className = "row mt-4") {
                div(className = "card mb-12") {
                    div(className = "card-body") {
                        div(className = "mb-3") {
                            h4("Password Settings")
                        }
                        div {
                            AppEngine.authService.whoAmI().then { userResponse ->
                                user = userResponse.data
                                userPasswordFormComponent(
                                    self = true,
                                    userPasswordUpdateForm = userPasswordUpdateForm,
                                ) {
                                    when (userPasswordUpdateForm.isValid()) {
                                        true -> {
                                            UserService.updateMyPassword(
                                                User(
                                                    id = user.id,
                                                    password = userPasswordUpdateForm.currentPassword.getValue(),
                                                    newPassword = userPasswordUpdateForm.newPassword.getValue(),
                                                    firstName = user.firstName,
                                                    lastName = user.lastName,
                                                    email = user.email,
                                                    roles = user.roles,
                                                ),
                                            ).then {
                                                sweetAlert.fire(
                                                    Json.encodeToDynamic(
                                                        mapOf(
                                                            "title" to "Success",
                                                            "text" to "Updated Password Successfully.",
                                                            "icon" to "success",
                                                        ),
                                                    ),
                                                )
                                                pageRefreshListner.setState(true)
                                            }.catch { throwable ->
                                                if (throwable is RemoteRequestException) {
                                                    if (throwable.code.toInt() == 400) {
                                                        throwable.response?.text()?.then {
                                                            val response: Map<String, String?> =
                                                                Json.decodeFromString(it)
                                                            return@then userPasswordFormErrorHandler(
                                                                response,
                                                                userPasswordUpdateForm,
                                                            )
                                                        }
                                                    }
                                                }
                                                userPasswordFormErrorHandler(null, userPasswordUpdateForm)
                                            }
                                        }
                                        false -> {
                                        }
                                    }
                                }
                            }.then {
                                window.setTimeout({
                                    window["feather"].replace()
                                }, 100)
                            }.catch {
                                sweetAlert.fire(
                                    Json.encodeToDynamic(
                                        mapOf(
                                            "title" to "Error",
                                            "text" to "Error while fetching user",
                                            "icon" to "error",
                                        ),
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    init {
        div {
            ObservableManager.setSubscriber("securitySettingsPage") {
                pageRefreshListner.subscribe {
                    if (it) {
                        removeAll()
                        add(contentBody())
                        SpaAppEngine.routing.updatePageLinks()
                        window.setTimeout({
                            window["feather"].replace()
                        }, 100)
                    }
                }
            }
            pageRefreshListner.setState(true)
        }
    }
}
