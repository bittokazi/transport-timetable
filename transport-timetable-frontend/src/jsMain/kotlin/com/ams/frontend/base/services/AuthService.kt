package com.ams.frontend.base.services

import com.ams.frontend.base.AppEngine
import com.ams.frontend.base.models.User
import com.bittokazi.kvision.spa.framework.base.common.AuthData
import com.bittokazi.kvision.spa.framework.base.common.AuthInformationProvider
import com.bittokazi.kvision.spa.framework.base.common.SpaAppEngine
import com.bittokazi.kvision.spa.framework.base.common.SpaAppEngine.defaultAuthHolder
import com.bittokazi.kvision.spa.framework.base.models.RefreshTokenRequestProvider
import com.bittokazi.kvision.spa.framework.base.models.SpaRole
import com.bittokazi.kvision.spa.framework.base.models.SpaUser
import com.bittokazi.kvision.spa.framework.base.services.LogoutActionProvider
import com.bittokazi.kvision.spa.framework.base.services.RestService
import io.kvision.rest.*
import kotlinx.browser.window
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToDynamic
import kotlinx.serialization.json.jsonPrimitive
import kotlin.js.Promise

class AuthService :
    AuthInformationProvider,
    LogoutActionProvider,
    RefreshTokenRequestProvider {
    var user: User? = null
    private val restService: RestService = SpaAppEngine.restService

    fun whoAmI(): Promise<RestResponse<User>> {
        return AppEngine.restService.createAuthCall {
            restService.getClient().request<User>("${restService.BASE_URL}/api/v1/users/whoami") {
                method = HttpMethod.GET
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun register(user: User): Promise<RestResponse<User>> {
        return restService.createAuthCall {
            restService.getClient().request<User>("${restService.BASE_URL}/api/public/v1/users") {
                method = HttpMethod.POST
                data = Json.encodeToDynamic(user)
            }
        }
    }

    override fun getAuthProvider(): Promise<SpaUser> {
        return Promise { resolve, reject ->
            whoAmI().then {
                user = it.data
                resolve(
                    SpaUser(
                        id = null,
                        email = it.data.email,
                        firstName = it.data.firstName,
                        lastName = it.data.lastName,
                        avatarImage = it.data.image,
                        spaRoles =
                            it.data.roles?.map { role ->
                                SpaRole(
                                    title = role.name,
                                    name = role.roleKey,
                                    id = role.id.toString(),
                                )
                            },
                    ),
                )
            }.catch { throwable ->
                reject(throwable)
            }
        }
    }

    override fun getRequest(): JsonObject {
        return JsonObject(
            mapOf(
                "refreshToken" to JsonPrimitive(defaultAuthHolder.getAuth()?.refreshToken ?: ""),
            ),
        )
    }

    override fun getAuthDataFromRefreshTokenResponse(response: JsonObject): AuthData {
        return AuthData(
            token = response["access_token"]?.jsonPrimitive?.content ?: "",
            refreshToken = response["refresh_token"]?.jsonPrimitive?.content ?: "",
        )
    }

    override fun logout(
        oauth2LoginPage: Boolean,
        fullLogout: Boolean,
    ) {
        window.location.href = "/oauth/logout"
    }
}
