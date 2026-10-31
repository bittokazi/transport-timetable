package com.ams.frontend.base.layout

import com.ams.frontend.base.AppEngine
import com.bittokazi.kvision.spa.framework.base.common.ObservableManager
import com.bittokazi.kvision.spa.framework.base.common.SpaAppEngine
import com.bittokazi.kvision.spa.framework.base.services.SpaTenantService
import com.bittokazi.kvision.spa.framework.base.services.SpaTenantService.tenantInfoObserver
import com.bittokazi.kvision.spa.framework.base.utils.sweetAlert
import io.kvision.core.Container
import io.kvision.core.onClick
import io.kvision.html.div
import io.kvision.html.footer
import io.kvision.html.li
import io.kvision.html.link
import io.kvision.html.span
import io.kvision.html.strong
import io.kvision.html.ul
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToDynamic

@OptIn(ExperimentalSerializationApi::class)
fun Container.customDashboardFooter() {
    footer(className = "footer") {
        div(className = "container-fluid") {
            div(className = "row text-muted g-2") {
                div(className = "col-12 col-md-6 text-center text-md-start") {
                    div(className = "d-inline-flex flex-wrap align-items-center justify-content-center justify-content-md-start gap-2 rounded-pill border bg-light px-3 py-2 shadow-sm") {
                        link("", SpaAppEngine.APP_DASHBOARD_ROUTE, dataNavigo = true) {
                            add(
                                strong(className = "text-dark fw-semibold") {
                                    content = SpaTenantService.tenantInfo.name
                                },
                            )
                        }
                        span(className = "text-muted") {
                            content = "&copy;"
                            rich = true
                        }
                    }
                }
                div(className = "col-12 col-md-6 text-center text-md-end") {
                    div(className = "d-inline-flex flex-wrap justify-content-center justify-content-md-end align-items-center gap-2 rounded-pill border bg-light px-2 px-md-3 py-2 shadow-sm text-muted small") {
                        span("System", className = "badge rounded-pill bg-secondary text-white")
                        span(AppEngine.infoService.systemInfo?.systemVersion ?: "N/A", className = "text-muted")
                        span("App", className = "badge rounded-pill bg-secondary text-white")
                        span(AppEngine.infoService.systemInfo?.appVersion ?: "N/A", className = "text-muted")
                        span("Build", className = "badge rounded-pill bg-secondary text-white")
                        span(AppEngine.infoService.systemInfo?.buildVersion ?: "N/A", className = "text-muted")
                        span("|", className = "text-muted px-1")
                        span(
                            "Change Log",
                            className = "btn btn-link btn-sm p-0 text-danger fw-semibold",
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
    }
}
