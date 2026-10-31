package com.bittokazi.watch.navigator.storage.service

import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveMultipart
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import com.bittokazi.watch.navigator.app.config.AppModuleConfiguration
import com.bittokazi.watch.navigator.persistence.entity.CallResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

interface FileService {
    fun loadTextFile(path: String): String?
}

class DefaultFileService(
    val appModuleConfiguration: AppModuleConfiguration,
) : FileService {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    init {
        log.info("[FileService] -> init")
    }

    override fun loadTextFile(path: String): String? {
        val file = File(path)

        // 1) Absolute or relative file on host filesystem
        if (file.exists()) {
            return file.readText()
        }

        // 2) Resource inside the JAR (from src/main/resources)
        val resourceStream =
            object {}.javaClass.classLoader.getResourceAsStream(path)
                ?: run {
                    log.error("Resource not found: $path")
                    return null
                }

        return resourceStream.bufferedReader().readText()
    }
}
