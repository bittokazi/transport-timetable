package com.bittokazi.watch.navigator.app.config

import io.ktor.server.plugins.di.annotations.Property
import kotlinx.serialization.Serializable

@Serializable
data class AppModuleConfiguration(
    @Property("app.modules") val modules: Map<AppModule, AppModuleConfig>,
    @Property("app.domain") val domain: String = "",
    @Property("app.name") val appName: String,
    @Property("app.enable-upgrade") val enableUpgrade: Boolean = true,
    @Property("app.enable-login") val enableLogin: Boolean = true,
)

@Serializable
enum class AppModule {
    USER,
    CPANEL,
    TRANSPORT,
    PASSWORD_RESET,
    ROLE,
    FEEDBACK,
}

@Serializable
data class AppModuleConfig(
    val enabled: Boolean,
)

@Serializable
data class AppInfo(
    val version: String,
    var changeLogs: List<String> = mutableListOf(),
    val appVersion: AppVersion = AppVersion(
        code = 0,
        name = "0.0.0_Dev",
        buildVersion = "app-v0.0.0"
    )
)

@Serializable
data class AppVersion(
    val code: Long,
    var name: String,
    val buildVersion: String,
)
