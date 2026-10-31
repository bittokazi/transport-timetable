package com.ams.frontend.app.secure.dashboard

import com.ams.frontend.base.AppEngine
import com.ams.frontend.base.models.RoleEnum
import io.kvision.core.UNIT
import io.kvision.html.TAG
import io.kvision.html.div
import io.kvision.html.h5
import io.kvision.html.h6
import io.kvision.html.image
import io.kvision.html.li
import io.kvision.html.link
import io.kvision.html.p
import io.kvision.html.span
import io.kvision.html.tag
import io.kvision.html.ul
import io.kvision.panel.SimplePanel

private const val SYSTEM_INFO_OBSERVER_VERSION = "systemInfoObserverVersion"

class DashboardHomePage() : SimplePanel() {
    init {
        div(className = "row") {
            // --- Info CARD ---
            AppEngine.authService.user?.roles
                ?.find { it.roleKey == RoleEnum.ROLE_USER.toString() }
                ?.let {
                    div(className = "col-md-5 col-xl-4") {
                        div(className = "card shadow-sm border-0 mb-4") {

                            // Card Header
                            div(
                                className = "card-header bg-transparent border-0 pt-4 pb-0 d-flex align-items-center justify-content-between",
                            ) {
                                h5(className = "card-title fw-bold mb-0 text-primary", content = "Usage Information")
                                span(className = "text-muted") {
                                    setAttribute("data-feather", "credit-card")
                                }
                            }

                            // Card Body
                            div(className = "card-body pt-3") {
                                div(className = "p-3 border border-primary-subtle bg-primary-subtle bg-opacity-10 rounded") {
                                    h6(className = "fw-bold text-primary small mb-1", content = "App Usage")
                                    p(className = "text-muted small mb-0") {
                                        content = "You can make ${AppEngine.infoService.systemInfo?.limit} trip plan calls " +
                                                "per month. "
                                    }
                                }
                            }
                        }
                    }
                }

            // --- PROFILE CARD ---
            div(className = "col-md-4 col-xl-3") {
                div(className = "card shadow-sm border-0 mb-4") {
                    div(className = "card-header bg-transparent border-0 pt-4 pb-0") {
                        h5(className = "card-title fw-bold mb-0", content = "Profile Details")
                    }
                    div(className = "card-body text-center pt-4") {
                        image(
                            src = AppEngine.authService.user!!.image,
                            className = "img-fluid rounded-circle mb-3 shadow-sm border border-2 border-light",
                        ) {
                            width = 128 to UNIT.px
                            height = 128 to UNIT.px
                        }
                        h5(className = "card-title fw-bold mb-1") {
                            content = "${AppEngine.authService.user!!.firstName} " +
                                "${AppEngine.authService.user!!.lastName}"
                        }
                        div(className = "text-muted small mb-3") {
                            content = AppEngine.authService.user?.roles?.firstOrNull()?.name
                        }
                    }
                    tag(TAG.HR, className = "my-0 opacity-25")
                    div(className = "card-body") {
                        h6(content = "About", className = "fw-bold card-title small text-uppercase text-muted mb-3")
                        ul(className = "list-unstyled mb-0") {
                            li(className = "mb-1 d-flex align-items-center gap-2") {
                                span(className = "feather-sm text-muted") {
                                    setAttribute("data-feather", "mail")
                                }
                                link(
                                    label = AppEngine.authService.user!!.email!!,
                                    url = "/app/dashboard",
                                    dataNavigo = true,
                                    className = "text-decoration-none text-reset small",
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
