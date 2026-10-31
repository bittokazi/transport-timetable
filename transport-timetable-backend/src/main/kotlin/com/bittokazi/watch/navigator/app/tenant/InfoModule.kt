package com.bittokazi.watch.navigator.app.tenant

import com.bittokazi.watch.navigator.app.config.AppInfo
import com.bittokazi.watch.navigator.app.tenant.controllers.infoRoute
import com.bittokazi.watch.navigator.app.tenant.services.DefaultInfoService
import com.bittokazi.watch.navigator.app.tenant.services.InfoService
import com.bittokazi.watch.navigator.storage.service.FileService
import com.google.gson.Gson
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies

fun Application.infoModule(): AppInfo {
    val fileService: FileService by dependencies

    var appInfo = AppInfo(version = "v0.0.0_Dev") // Default version

    fileService.loadTextFile("/app/info.json")?.let { json ->
        appInfo = Gson().fromJson(json, AppInfo::class.java)
    } ?: run {
        fileService.loadTextFile("info.json")?.let { json ->
            appInfo = Gson().fromJson(json, AppInfo::class.java)
        }
    }

    dependencies {
        provide<AppInfo> {
            appInfo
        }
    }

    dependencies {
        provide<InfoService>(DefaultInfoService::class)
    }

    infoRoute()

    return appInfo
}
