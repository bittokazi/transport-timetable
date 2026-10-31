package com.ams.frontend.app.open

import com.bittokazi.kvision.spa.framework.base.common.SpaAppEngine
import io.kvision.html.link
import io.kvision.html.p
import io.kvision.panel.SimplePanel

class HomePage : SimplePanel() {
    init {
        SpaAppEngine.routing.navigate("/app/login")
        p {
            link("Login Page", "/app/login", dataNavigo = true)
        }
    }
}
