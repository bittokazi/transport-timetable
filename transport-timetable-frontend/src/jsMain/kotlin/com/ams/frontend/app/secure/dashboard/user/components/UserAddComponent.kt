package com.ams.frontend.app.secure.dashboard.user.components

import com.ams.frontend.app.secure.dashboard.role.RoleService
import com.ams.frontend.app.secure.dashboard.user.UserService
import com.ams.frontend.app.secure.dashboard.user.components.form.UserForm
import com.ams.frontend.app.secure.dashboard.user.components.form.userFormComponent
import com.ams.frontend.app.secure.dashboard.user.components.form.userFormErrorHandler
import com.ams.frontend.base.AppEngine.APP_DASHBOARD_USER_ROUTE
import com.ams.frontend.base.models.Role
import com.ams.frontend.base.models.User
import com.bittokazi.kvision.spa.framework.base.common.SpaAppEngine
import com.bittokazi.kvision.spa.framework.base.utils.sweetAlert
import io.kvision.html.div
import io.kvision.panel.SimplePanel
import io.kvision.rest.RemoteRequestException
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToDynamic

@OptIn(ExperimentalSerializationApi::class)
class UserAddComponent : SimplePanel() {
    val userForm = UserForm(update = false)

    init {
        div {
            RoleService.getAll().then { roleList ->
                userFormComponent(
                    userForm = userForm,
                    user = null,
                    rolesList = roleList.data,
                ) {
                    when (userForm.isValid()) {
                        true -> {
                            UserService.create(
                                User(
                                    firstName = userForm.firstName.getValue(),
                                    lastName = userForm.lastName.getValue(),
                                    email = userForm.email.getValue(),
                                    roles =
                                        listOf(
                                            Role(
                                                id = userForm.roles.getValue()?.toLongOrNull(),
                                            ),
                                        ),
                                    newPassword = userForm.newPassword.getValue(),
                                ),
                            ).then {
                                sweetAlert.fire(
                                    Json.encodeToDynamic(
                                        mapOf(
                                            "title" to "Success",
                                            "text" to "Added User Successfully.",
                                            "icon" to "success",
                                        ),
                                    ),
                                )
                                SpaAppEngine.routing.navigate(APP_DASHBOARD_USER_ROUTE)
                            }.catch { throwable ->
                                if (throwable is RemoteRequestException) {
                                    if (throwable.code.toInt() == 400) {
                                        throwable.response?.text()?.then {
                                            val response: Map<String, String?> = Json.decodeFromString(it)
                                            return@then userFormErrorHandler(response, userForm)
                                        }
                                    }
                                }
                                console.log(throwable)
                                userFormErrorHandler(null, userForm)
                            }
                        }
                        false -> {
                        }
                    }
                }
            }.catch {
                sweetAlert.fire(
                    Json.encodeToDynamic(
                        mapOf(
                            "title" to "Error",
                            "text" to "Error while fetching roles",
                            "icon" to "error",
                        ),
                    ),
                )
            }
        }
    }
}
