package com.bittokazi.watch.navigator.storage

import com.bittokazi.watch.navigator.storage.service.DefaultFileService
import com.bittokazi.watch.navigator.storage.service.FileService
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies

fun Application.configureStorageModule() {
    dependencies {
        provide<FileService>(DefaultFileService::class)
    }
}
