package com.bittokazi.watch.navigator.security.config

import com.bittokazi.ktor.auth.services.SessionExtender
import io.ktor.server.sessions.SessionsConfig

class SessionExtenderImpl : SessionExtender {
    override fun extent(sessionsConfig: SessionsConfig) {
    }
}
