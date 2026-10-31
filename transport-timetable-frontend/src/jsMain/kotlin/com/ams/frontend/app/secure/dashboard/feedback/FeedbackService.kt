package com.ams.frontend.app.secure.dashboard.feedback

import com.ams.frontend.base.AppEngine.restService
import com.ams.frontend.base.models.BaseList
import com.ams.frontend.base.models.Feedback
import io.kvision.rest.HttpMethod
import io.kvision.rest.RestResponse
import io.kvision.rest.request
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToDynamic
import kotlin.js.Promise

@Serializable
data class FeedbackCreateRequest(val content: String)

object FeedbackService {
    private val feedbackBaseUrl = "${restService.BASE_URL}/api/v1/feedback"

    fun getAll(
        page: Int = 1,
        count: Int = 10,
        query: String = "",
    ): Promise<RestResponse<BaseList<Feedback>>> {
        return restService.createAuthCall {
            restService.getClient().request<BaseList<Feedback>>("$feedbackBaseUrl?page=$page&count=$count&query=$query") {
                method = HttpMethod.GET
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun create(feedback: Feedback): Promise<RestResponse<Feedback>> {
        return restService.createAuthCall {
            restService.getClient().request<Feedback>(feedbackBaseUrl) {
                method = HttpMethod.POST
                data = Json.encodeToDynamic(FeedbackCreateRequest(content = feedback.content))
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun resolve(id: Long): Promise<RestResponse<Feedback>> {
        return restService.createAuthCall {
            restService.getClient().request<Feedback>("$feedbackBaseUrl/$id/resolve") {
                method = HttpMethod.PUT
                data = Json.encodeToDynamic(mapOf<String, String>())
            }
        }
    }
}

