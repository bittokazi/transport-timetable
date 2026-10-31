package com.bittokazi.watch.navigator.config

object RouteConfig {
    const val API = "/api"
    const val V1 = "v1"
    const val API_PUBLIC = "/api/public"

    // OAuth API
    const val LOGIN_API = "/login"
    const val REFRESH_TOKEN_API = "/oauth/refresh/token"
    const val OAUTH_CALLBACK_API = "/oauth/callback"

    // User API
    const val API_USER = "$API/$V1/users"
    const val API_USER_BY_ID = "$API_USER/{id}"
    const val API_USER_WHO_AM_I = "$API_USER/whoami"
    const val API_USER_WHO_AM_I_UPDATE_PASSWORD = "$API_USER/whoami/update-password"
    const val API_USER_UPDATE_PASSWORD = "$API_USER_BY_ID/update-password"
    const val API_USER_REGISTER = "$API_PUBLIC/$V1/users"

    // Feedback API
    const val API_FEEDBACK = "$API/$V1/feedback"
    const val API_FEEDBACK_BY_ID = "$API_FEEDBACK/{id}"
    const val API_FEEDBACK_RESOLVE = "$API_FEEDBACK_BY_ID/resolve"

    // Role API
    const val API_ROLE = "$API/$V1/roles"

    // Info API
    const val API_INFO = "$API/$V1/info"
}
