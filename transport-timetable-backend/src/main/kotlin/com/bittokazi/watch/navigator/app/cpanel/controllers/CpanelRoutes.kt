package com.bittokazi.watch.navigator.app.cpanel.controllers

import com.bittokazi.ktor.auth.services.DefaultTemplateCustomizerFactory
import com.bittokazi.ktor.auth.services.TemplateCustomizerFactory
import com.github.mustachejava.DefaultMustacheFactory
import io.ktor.http.ContentType
import io.ktor.server.application.*
import io.ktor.server.http.content.staticResources
import io.ktor.server.mustache.*
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.StringWriter

fun Application.cpanelRoutes() {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    val templateCustomizerFactory: TemplateCustomizerFactory by dependencies

    routing {
        staticResources("/app-static", "static/cpanel")
    }

    routing {
        head("/app") {
            respondCpanelTemplate(
                templateCustomizerFactory,
                call,
                "cpanel.hbs",
                mapOf<String, Any>(),
            )
        }
        get("/app") {
            respondCpanelTemplate(
                templateCustomizerFactory,
                call,
                "cpanel.hbs",
                mapOf<String, Any>(),
            )
        }
        get("/app/") {
            respondCpanelTemplate(
                templateCustomizerFactory,
                call,
                "cpanel.hbs",
                mapOf<String, Any>(),
            )
        }
        get("/app/{...}") {
            respondCpanelTemplate(
                templateCustomizerFactory,
                call,
                "cpanel.hbs",
                mapOf<String, Any>(),
            )
        }
    }

    log.info("[cpanelRoutes] -> Routes configured.")
}

private suspend fun respondCpanelTemplate(
    factory: TemplateCustomizerFactory,
    call: ApplicationCall,
    template: String,
    model: Any,
) {
    val html =
        StringWriter().also {
            factory.getFactory(call).compile(template)
                .execute(it, model)
                .flush()
        }.toString()

    call.respondText(html, ContentType.Text.Html)
}
