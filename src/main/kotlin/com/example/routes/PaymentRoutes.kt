package com.example.routes

import com.example.models.*
import com.example.repositories.PaymentRepository
import io.github.smiley4.ktorswaggerui.dsl.routing.get
import io.github.smiley4.ktorswaggerui.dsl.routing.post
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class PaymentRoutes(private val paymentRepository: PaymentRepository) {

    fun Route.paymentRoutes() {
        route("/payments") {
            post("/process", {
                tags = listOf("Payment")
                summary = "Process a new payment"
                description = "Process payment for an order"
                request {
                    body<PaymentRequest> {
                        description = "Payment details"
                    }
                }
                response {
                    HttpStatusCode.Created to {
                        description = "Payment processed successfully"
                        body<Payment> { description = "Payment details" }
                    }
                    HttpStatusCode.BadRequest to {
                        description = "Invalid payment data"
                    }
                }
            }) {
                try {
                    val request = call.receive<PaymentRequest>()
                    val payment = Payment(
                        orderId = request.orderId,
                        amount = request.amount,
                        paymentMethod = request.paymentMethod,
                        paymentStatus = PaymentStatus.COMPLETED,
                        transactionReference = request.transactionReference
                    )
                    val createdPayment = paymentRepository.createPayment(payment)
                    call.respond(HttpStatusCode.Created, createdPayment)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to (e.message ?: "Failed to process payment"))
                    )
                }
            }

            get("/{orderId}", {
                tags = listOf("Payment")
                summary = "Get payment by order ID"
                description = "Retrieve payment details for a specific order"
                request {
                    pathParameter<Int>("orderId") {
                        description = "ID of the order"
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = "Payment found"
                        body<Payment> { description = "Payment details" }
                    }
                    HttpStatusCode.NotFound to {
                        description = "Payment not found"
                    }
                }
            }) {
                try {
                    val orderId = call.parameters["orderId"]?.toIntOrNull()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid order ID")

                    val payment = paymentRepository.getPaymentByOrderId(orderId)
                    if (payment != null) {
                        call.respond(HttpStatusCode.OK, payment)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Payment not found")
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Failed to retrieve payment"))
                    )
                }
            }

            get("/table/{tableId}", {
                tags = listOf("Payment")
                summary = "Get table payment summary"
                description = "Get payment details for a specific table"
                request {
                    pathParameter<Int>("tableId") {
                        description = "Table ID"
//                        example("1")
                    }
                    queryParameter<String>("sessionId") {
                        description = "Session ID (optional - defaults to active session)"
                        required = false
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = "Payment summary found"
                        body<TablePaymentSummary> { 
                            description = "Table payment summary"
                            example("Payment Summary") {
                                value = mapOf(
                                    "tableId" to 1,
                                    "tableNumber" to 1,
                                    "sessionId" to "T1_20240318_123456",
                                    "totalAmount" to 1000.0,
                                    "paidAmount" to 500.0,
                                    "remainingAmount" to 500.0,
                                    "paymentStatus" to "PENDING",
                                    "payments" to listOf(
                                        mapOf(
                                            "id" to 1,
                                            "orderId" to 1,
                                            "amount" to 500.0,
                                            "paymentMethod" to "CREDIT_CARD",
                                            "paymentStatus" to "COMPLETED",
                                            "createdAt" to "2024-03-18T14:30:00"
                                        )
                                    )
                                )
                            }
                        }
                    }
                    HttpStatusCode.NotFound to {
                        description = "Table not found or no active session"
                    }
                }
            }) {
                try {
                    val tableId = call.parameters["tableId"]?.toIntOrNull()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid table ID")
                    
                    val sessionId = call.parameters["sessionId"]
                    
                    val summary = paymentRepository.getTablePayments(tableId, sessionId)
                    call.respond(HttpStatusCode.OK, summary)
                } catch (e: Exception) {
                    when (e.message) {
                        "Table not found" -> call.respond(HttpStatusCode.NotFound, "Table not found")
                        "No active session found" -> call.respond(HttpStatusCode.NotFound, "No active session found")
                        else -> call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to (e.message ?: "Failed to retrieve payment summary"))
                        )
                    }
                }
            }
        }
    }
} 