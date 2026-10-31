package com.ams.frontend.app.secure.dashboard.user

import com.ams.frontend.base.AppEngine.restService
import com.ams.frontend.base.models.BaseList
import com.ams.frontend.base.models.User
import io.kvision.rest.HttpMethod
import io.kvision.rest.RestResponse
import io.kvision.rest.request
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToDynamic
import kotlin.js.Promise

object UserService {
    private val userBaseUrl = "${restService.BASE_URL}/api/v1/users"

    fun getAll(
        page: Int = 1,
        count: Int = 10,
        query: String = ""
    ): Promise<RestResponse<BaseList<User>>> {
        return restService.createAuthCall {
            restService.getClient().request<BaseList<User>>("$userBaseUrl?page=$page&count=$count&query=$query") {
                method = HttpMethod.GET
            }
        }
    }

    fun get(id: String): Promise<RestResponse<User>> {
        return restService.createAuthCall {
            restService.getClient().request<User>("$userBaseUrl/$id") {
                method = HttpMethod.GET
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun create(user: User): Promise<RestResponse<User>> {
        return restService.createAuthCall {
            restService.getClient().request<User>(userBaseUrl) {
                method = HttpMethod.POST
                data = Json.encodeToDynamic(user)
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun update(user: User): Promise<RestResponse<User>> {
        return restService.createAuthCall {
            restService.getClient().request<User>("$userBaseUrl/${user.id}") {
                method = HttpMethod.PUT
                data = Json.encodeToDynamic(user)
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun updatePassword(user: User): Promise<RestResponse<User>> {
        return restService.createAuthCall {
            restService.getClient().request<User>("$userBaseUrl/${user.id}/update-password") {
                method = HttpMethod.PUT
                data = Json.encodeToDynamic(user)
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun updateMyProfile(user: User): Promise<RestResponse<User>> {
        return restService.createAuthCall {
            restService.getClient().request<User>("$userBaseUrl/whoami") {
                method = HttpMethod.PUT
                data = Json.encodeToDynamic(user)
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun updateMyPassword(user: User): Promise<RestResponse<User>> {
        return restService.createAuthCall {
            restService.getClient().request<User>("$userBaseUrl/whoami/update-password") {
                method = HttpMethod.PUT
                data = Json.encodeToDynamic(user)
            }
        }
    }
}
