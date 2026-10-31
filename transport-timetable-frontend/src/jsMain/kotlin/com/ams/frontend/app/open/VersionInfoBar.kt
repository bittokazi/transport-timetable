package com.ams.frontend.app.open

import com.ams.frontend.base.AppEngine
import com.bittokazi.kvision.spa.framework.base.utils.sweetAlert
import io.kvision.core.onClick
import io.kvision.html.Div
import io.kvision.html.div
import io.kvision.html.span
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToDynamic

@OptIn(ExperimentalSerializationApi::class)
class VersionInfoBar : Div() {
    init {
        div(className = "text-center pt-3 pb-2") {
            div(className = "d-inline-flex flex-wrap justify-content-center align-items-center gap-2 rounded-pill border bg-light px-3 py-2 shadow-sm") {
                span("System", className = "badge rounded-pill bg-secondary text-white")
                span(AppEngine.infoService.systemInfo?.systemVersion ?: "N/A", className = "text-muted small")
                span("App", className = "badge rounded-pill bg-secondary text-white")
                span(AppEngine.infoService.systemInfo?.appVersion ?: "N/A", className = "text-muted small")
                span("Build", className = "badge rounded-pill bg-secondary text-white")
                span(AppEngine.infoService.systemInfo?.buildVersion ?: "N/A", className = "text-muted small")
                span(
                    "Change Log",
                    className = "btn btn-link btn-sm p-0 ms-1 text-danger fw-semibold",
                ).onClick {
                    sweetAlert.fire(
                        Json.encodeToDynamic(
                            mapOf(
                                "title" to "Change log",
                                "html" to AppEngine.infoService.getChangeLogs(),
                                "icon" to "info",
                                "confirmButtonText" to "Close",
                                "width" to "600px",
                            ),
                        ),
                    )
                }
            }
        }
    }
}
