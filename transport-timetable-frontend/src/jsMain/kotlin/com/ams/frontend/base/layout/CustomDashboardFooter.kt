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
import io.kvision.html.i
import io.kvision.html.link
import io.kvision.html.span
import io.kvision.html.strong
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToDynamic

@OptIn(ExperimentalSerializationApi::class)
fun Container.customDashboardFooter() {
    footer(className = "footer") {
        div(className = "container-fluid") {
            div(className = "row text-muted g-2 align-items-center") {

                // 1. Left Pill: Tenant Name
                div(className = "col-12 col-lg-3 text-center text-lg-start") {
                    div(className = "d-inline-flex flex-wrap align-items-center justify-content-center justify-content-lg-start gap-2 rounded-pill border bg-light px-3 py-2 shadow-sm") {
                        link("", SpaAppEngine.APP_DASHBOARD_ROUTE, dataNavigo = true, className = "text-decoration-none") {
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

                // 2. Middle Pill: Data Sources & Attribution
                div(className = "col-12 col-lg-5 text-center") {
                    div(className = "d-inline-flex justify-content-center align-items-center gap-1 rounded-pill border bg-light px-3 py-2 shadow-sm text-secondary text-nowrap") {
                        i(className = "bi bi-geo-alt-fill text-primary me-1")
                        span("Transit data provided by")
                        link(
                            label = "Transitous",
                            url = "https://transitous.org/sources/",
                            className = "fw-bold text-decoration-none text-primary",
                            target = "_blank",
                        ).apply {
                            setAttribute("rel", "noopener noreferrer")
                            setAttribute("title", "Transitous data sources and licensing information")
                        }

                        span(className = "d-inline-flex align-items-center gap-1 ms-1") {
                            span("& map contributors")
                            link(
                                label = "© OpenStreetMap",
                                url = "https://www.openstreetmap.org/copyright",
                                className = "fw-semibold text-decoration-none text-secondary",
                                target = "_blank",
                            ).apply {
                                setAttribute("rel", "noopener noreferrer")
                                setAttribute("title", "OpenStreetMap copyright and license information")
                            }
                        }
                    }
                }

                // 3. Right Pill: System / App Version & Change Log
                div(className = "col-12 col-lg-4 text-center text-lg-end") {
                    div(className = "d-inline-flex flex-wrap justify-content-center justify-content-lg-end align-items-center gap-2 rounded-pill border bg-light px-3 py-2 shadow-sm text-muted") {
                        span("System", className = "badge rounded-pill bg-secondary text-white")
                        span(AppEngine.infoService.systemInfo?.systemVersion ?: "N/A", className = "text-muted small")
                        span("App", className = "badge rounded-pill bg-secondary text-white")
                        span(AppEngine.infoService.systemInfo?.appVersion ?: "N/A", className = "text-muted small")
                        span("Build", className = "badge rounded-pill bg-secondary text-white")
                        span(AppEngine.infoService.systemInfo?.buildVersion ?: "N/A", className = "text-muted small")
                        span("|", className = "text-muted px-1")
                        span(
                            "Change Log",
                            className = "btn btn-link btn-sm p-0 text-danger fw-semibold text-decoration-none",
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
