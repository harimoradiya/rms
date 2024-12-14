package com.example.routes

import com.example.services.InvoiceService
import io.github.smiley4.ktorswaggerui.dsl.routing.get
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class InvoiceRoutes(private val invoiceService: InvoiceService) {

    fun Route.invoiceRoutes() {
        route("/invoices") {
            get("/table/{tableId}/session/{sessionId}", {
                tags = listOf("Invoice")
                summary = "Generate table session invoice"
                description = "Generate a PDF invoice for all orders in a table session"
                request {
                    pathParameter<Int>("tableId") {
                        description = "Table ID"
                    }
                    pathParameter<String>("sessionId") {
                        description = "Session ID"
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = "PDF invoice generated successfully"
                        body<ByteArray> { description = "PDF document" }
                    }
                    HttpStatusCode.NotFound to {
                        description = "Table or session not found"
                    }
                }
            }) {
                try {
                    val tableId = call.parameters["tableId"]?.toIntOrNull()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid table ID")
                    val sessionId = call.parameters["sessionId"]
                        ?: return@get call.respond(HttpStatusCode.BadRequest, "Session ID required")
                    val pdfBytes = invoiceService.generateTableSessionInvoice(tableId, sessionId)
                    
                    call.response.header(
                        HttpHeaders.ContentDisposition,
                        "attachment; filename=\"table-$tableId-session-$sessionId.pdf\""
                    )
                    call.respondBytes(
                        pdfBytes,
                        ContentType.Application.Pdf,
                        HttpStatusCode.OK
                    )
                } catch (e: Exception) {
                    when (e.message) {
                        "Table not found" -> call.respond(HttpStatusCode.NotFound, "Table not found")
                        "No orders found for this table session" -> 
                            call.respond(HttpStatusCode.NotFound, "No orders found for this session")
                        else -> call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to (e.message ?: "Failed to generate invoice"))
                        )
                    }
                }
            }
        }
    }
} 