package com.ams.frontend.app.open

import com.ams.frontend.base.AppEngine
import com.bittokazi.kvision.spa.framework.base.utils.sweetAlert
import io.kvision.core.onClick
import io.kvision.html.Div
import io.kvision.html.div
import io.kvision.html.i
import io.kvision.html.link
import io.kvision.html.span
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToDynamic

@OptIn(ExperimentalSerializationApi::class)
class VersionInfoBar : Div() {
    init {
        // Main container with increased vertical gap (gap-3)
        div(className = "text-center pt-3 pb-3 px-2 d-flex flex-column align-items-center gap-3") {

            // 1. Primary Version & Change Log Bar
            div(className = "d-inline-flex flex-wrap justify-content-center align-items-center gap-2 rounded-pill border bg-light px-3 py-1.5 shadow-sm text-nowrap") {
                span("System", className = "badge rounded-pill bg-secondary text-white")
                span(AppEngine.infoService.systemInfo?.systemVersion ?: "N/A", className = "text-muted small")

                span("App", className = "badge rounded-pill bg-secondary text-white")
                span(AppEngine.infoService.systemInfo?.appVersion ?: "N/A", className = "text-muted small")

                span("Build", className = "badge rounded-pill bg-secondary text-white")
                span(AppEngine.infoService.systemInfo?.buildVersion ?: "N/A", className = "text-muted small")

                span(
                    "Change Log",
                    className = "btn btn-link btn-sm p-0 ms-1 text-danger fw-semibold text-decoration-none",
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

            // 2. Separate Data Attribution Bar (Added mt-1 for extra vertical separation)
            div(className = "d-inline-flex flex-wrap justify-content-center align-items-center gap-1 rounded-pill border bg-light px-3 py-1.5 shadow-sm text-nowrap") {
                // Map/Globe Icon
                span("\uD83D\uDE86 Transit data provided by")

                // Transitous Link
                link(
                    label = "Transitous",
                    url = "https://transitous.org/sources/",
                    className = "fw-bold text-decoration-none text-primary",
                    target = "_blank",
                ).apply {
                    setAttribute("rel", "noopener noreferrer")
                    setAttribute("title", "Transitous data sources and licensing information")
                }
            }

            div(className = "d-inline-flex flex-wrap justify-content-center align-items-center gap-1 rounded-pill border bg-light px-3 py-1.5 shadow-sm text-nowrap") {
                // Inline span to keep attribution on a single line
                span(className = "d-inline-flex align-items-center gap-1 ms-1") {
                    span("\uD83C\uDF0D Map contributors")
                    span("(")
                    link(
                        label = "© OpenStreetMap",
                        url = "https://www.openstreetmap.org/copyright",
                        className = "fw-semibold text-decoration-none text-secondary",
                        target = "_blank",
                    ).apply {
                        setAttribute("rel", "noopener noreferrer")
                        setAttribute("title", "OpenStreetMap copyright and license information")
                    }
                    span(")")
                }
            }
        }
    }
}
