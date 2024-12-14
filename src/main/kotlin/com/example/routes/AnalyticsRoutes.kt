package com.example.routes

import com.example.models.*
import com.example.repositories.AnalyticsRepository
import io.github.smiley4.ktorswaggerui.dsl.routing.get
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class AnalyticsRoutes(private val analyticsRepository: AnalyticsRepository) {

    fun Route.analyticsRoutes() {
        route("/analytics") {
            get("/dashboard", {
                tags = listOf("Analytics")
                summary = "Get complete sales analytics dashboard"
                description = "Retrieve comprehensive sales analytics including summaries, top items, and trends"
                request {
                    queryParameter<String>("startDate") {
                        description = "Start date (ISO format)"
                        required = true
                    }
                    queryParameter<String>("endDate") {
                        description = "End date (ISO format)"
                        required = true
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = "Analytics dashboard retrieved successfully"
                        body<SalesAnalyticsDashboard> { description = "Complete analytics dashboard" }
                    }
                }
            }) {
                try {
                    val startDate = call.parameters["startDate"]
                        ?: return@get call.respond(HttpStatusCode.BadRequest, "Start date is required")
                    val endDate = call.parameters["endDate"]
                        ?: return@get call.respond(HttpStatusCode.BadRequest, "End date is required")

                    val dashboard = analyticsRepository.getCompleteDashboard(startDate, endDate)
                    call.respond(HttpStatusCode.OK, dashboard)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Failed to retrieve analytics"))
                    )
                }
            }

            get("/top-selling", {
                tags = listOf("Analytics")
                summary = "Get top selling items"
                description = "Retrieve list of top selling menu items"
                request {
                    queryParameter<String>("startDate") {
                        description = "Start date (ISO format)"
                        required = true
                    }
                    queryParameter<String>("endDate") {
                        description = "End date (ISO format)"
                        required = true
                    }
                    queryParameter<Int>("limit") {
                        description = "Number of items to retrieve"
                        required = false

//                        defaultValue = 10
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = "Top selling items retrieved successfully"
                        body<List<ItemSalesReport>> { description = "List of top selling items" }
                    }
                }
            }) {
                try {
                    val startDate = call.parameters["startDate"]
                        ?: return@get call.respond(HttpStatusCode.BadRequest, "Start date is required")
                    val endDate = call.parameters["endDate"]
                        ?: return@get call.respond(HttpStatusCode.BadRequest, "End date is required")
                    val limit = call.parameters["limit"]?.toIntOrNull() ?: 10

                    val topItems = analyticsRepository.getTopSellingItems(startDate, endDate, limit)
                    call.respond(HttpStatusCode.OK, topItems)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Failed to retrieve top selling items"))
                    )
                }
            }

            get("/payment-methods", {
                tags = listOf("Analytics")
                summary = "Get payment method breakdown"
                description = "Retrieve analysis of payment methods used"
                request {
                    queryParameter<String>("startDate") {
                        description = "Start date (ISO format)"
                        required = true
                    }
                    queryParameter<String>("endDate") {
                        description = "End date (ISO format)"
                        required = true
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = "Payment method breakdown retrieved successfully"
                        body<List<PaymentMethodSummary>> { description = "Payment method analysis" }
                    }
                }
            }) {
                try {
                    val startDate = call.parameters["startDate"]
                        ?: return@get call.respond(HttpStatusCode.BadRequest, "Start date is required")
                    val endDate = call.parameters["endDate"]
                        ?: return@get call.respond(HttpStatusCode.BadRequest, "End date is required")

                    val breakdown = analyticsRepository.getPaymentMethodBreakdown(startDate, endDate)
                    call.respond(HttpStatusCode.OK, breakdown)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Failed to retrieve payment method breakdown"))
                    )
                }
            }
        }
    }
} 