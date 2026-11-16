package com.bittokazi.watch.navigator.validation.services

import com.bittokazi.watch.navigator.validation.config.ValidationConfiguration
import java.math.BigInteger

interface VersionValidationService {
    fun isVersionValid(
        appVersion: String?,
    ): Boolean
}

class DefaultVersionValidationService(
    private val validationConfiguration: ValidationConfiguration
): VersionValidationService {
    override fun isVersionValid(appVersion: String?): Boolean {
        return when (validationConfiguration.versionEnabled) {
            true -> {
                when (appVersion) {
                    null -> false
                    else -> {
                        val appParts = parseVersion(appVersion)
                        val minParts = validationConfiguration.minVersion?.let(::parseVersion)

                        if (appParts == null || minParts == null) {
                            false
                        } else {
                            var comparison = 0
                            for (index in appParts.indices) {
                                comparison = appParts[index].compareTo(minParts[index])
                                if (comparison != 0) break
                            }
                            comparison >= 0
                        }
                    }
                }
            }
            false -> true
        }
    }

    private fun parseVersion(version: String): List<BigInteger>? {
        val parts = version.split('.')
        if (parts.size != 3 || parts.any { part -> part.isEmpty() || part.any { !it.isDigit() } }) {
            return null
        }
        return parts.map { it.toBigIntegerOrNull() ?: return null }
    }
}
