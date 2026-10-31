package com.bittokazi.watch.navigator.persistence.entity

data class PaginationRequest(
    val page: Int?,
    val count: Int?,
    val query: String?,
)
