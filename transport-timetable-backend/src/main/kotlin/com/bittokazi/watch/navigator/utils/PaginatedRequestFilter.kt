package com.bittokazi.watch.navigator.utils

import com.bittokazi.watch.navigator.persistence.entity.PaginationRequest
import io.ktor.server.application.ApplicationCall
import kotlin.text.toInt

suspend fun paginatedRequestFilter(
    call: ApplicationCall,
    function: suspend (PaginationRequest) -> Unit,
) {
    val paginationRequest =
        PaginationRequest(
            page = call.request.queryParameters["page"]?.toInt() ?: 1,
            count = call.request.queryParameters["count"]?.toInt() ?: 1,
            query = call.request.queryParameters["query"] ?: "",
        )

    function(paginationRequest)
}
