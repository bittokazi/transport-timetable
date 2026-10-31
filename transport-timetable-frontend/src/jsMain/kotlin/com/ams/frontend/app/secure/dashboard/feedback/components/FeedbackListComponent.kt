package com.ams.frontend.app.secure.dashboard.feedback.components

import com.ams.frontend.app.secure.dashboard.feedback.FeedbackService
import com.ams.frontend.base.models.BaseList
import com.ams.frontend.base.models.Feedback
import com.ams.frontend.base.models.FeedbackStatus
import com.bittokazi.kvision.spa.framework.base.common.SpaAppEngine.routing
import com.bittokazi.kvision.spa.framework.base.components.form.FormSelectInput
import com.bittokazi.kvision.spa.framework.base.components.form.selectInputComponent
import com.bittokazi.kvision.spa.framework.base.components.pagination.paginationComponent
import io.kvision.core.onChange
import io.kvision.html.Div
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.html.span
import io.kvision.html.table
import io.kvision.html.tbody
import io.kvision.html.td
import io.kvision.html.th
import io.kvision.html.thead
import io.kvision.html.tr
import io.kvision.panel.SimplePanel
import io.kvision.state.ObservableValue
import kotlinx.browser.window
import org.w3c.dom.get

class FeedbackListComponent : SimplePanel() {
    val listObserver = ObservableValue<BaseList<Feedback>?>(null)

    var page = 1
    val count = 10
    var statusFilter = ""
    private val statusFilterInput =
        FormSelectInput(
            label = "Status",
            defaultInvalidFeedback = "Status is required",
        ) { true }

    fun getFeedbacks() {
        val query = if (statusFilter.isNotEmpty()) "status=$statusFilter" else ""
        FeedbackService.getAll(
            page = page,
            count = count,
            query = query,
        ).then {
            listObserver.setState(it.data)
        }
    }

    fun tableContent(feedbackList: BaseList<Feedback>): Div {
        return Div(className = "row mb-3") {
            div(className = "table-responsive") {
                table(className = "table table-hover my-0") {
                    thead {
                        tr {
                            th { content = "#" }
                            th { content = "User" }
                            th { content = "Email" }
                            th { content = "Status" }
                            th { content = "Created" }
                            th { content = "Content" }
                            th { content = "Action" }
                        }
                    }

                    tbody {
                        feedbackList.data.forEach { feedback ->
                            tr {
                                td { content = feedback.id.toString() }
                                td { content = "${feedback.user.firstName} ${feedback.user.lastName}" }
                                td { content = feedback.user.email }
                                td { content = feedback.status.toString() }
                                td { content = feedback.createdAt ?: "-" }
                                td { content = feedback.content }
                                td {
                                    if (feedback.status == FeedbackStatus.CREATED) {
                                        button(text = "Resolve", className = "btn btn-sm btn-primary") {
                                            onClick {
                                                FeedbackService.resolve(feedback.id!!).then {
                                                    getFeedbacks()
                                                }
                                            }
                                        }
                                    } else {
                                        span(className = "text-success") { content = "Resolved" }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun pagination(feedbackList: BaseList<Feedback>): Div {
        return div(className = "row") {
            add(
                paginationComponent(
                    when (page > 1) { true -> page - 1; false -> null },
                    when (page < feedbackList.totalPage.toInt()) { true -> page + 1; false -> null },
                    page,
                    feedbackList.totalPage.toInt(),
                    feedbackList.totalCount.toInt(),
                    count,
                ) { pageNumber ->
                    page = pageNumber
                    getFeedbacks()
                },
            )
        }
    }

    init {
        div {
            div(className = "row mb-3") {
                div(className = "col-md-3") {
                    add(
                        selectInputComponent(
                            statusFilterInput,
                            listOf(
                                "" to "All",
                                FeedbackStatus.CREATED.name to FeedbackStatus.CREATED.name,
                                FeedbackStatus.RESOLVED.name to FeedbackStatus.RESOLVED.name
                            ),
                            "",
                        ),
                    )
                }
            }

            statusFilterInput.input.onChange {
                statusFilter = statusFilterInput.getValue() ?: ""
                page = 1
                getFeedbacks()
            }
        }
        div {
            listObserver.subscribe {
                if (it != null) {
                    removeAll()
                    add(tableContent(it))
                    add(pagination(it))
                    routing.updatePageLinks()
                    window.setTimeout({
                        window["feather"].replace()
                    }, 100)
                }
            }
            getFeedbacks()
        }
    }
}
