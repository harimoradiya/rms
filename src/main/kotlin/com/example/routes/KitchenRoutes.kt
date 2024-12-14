package com.example.routes

import com.example.models.*
import com.example.repositories.KitchenRepository
import io.github.smiley4.ktorswaggerui.dsl.routing.get
import io.github.smiley4.ktorswaggerui.dsl.routing.post
import io.github.smiley4.ktorswaggerui.dsl.routing.put
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class KitchenRoutes(private val kitchenRepository: KitchenRepository) {

    fun Route.kitchenRoutes() {
        route("/kitchen") {

            get("/orders", {
                tags = listOf("Kitchen")
                summary = "Get active kitchen orders"
                description = "Retrieve all active orders in the kitchen"
                response {
                    HttpStatusCode.OK to {
                        description = "List of active kitchen orders"
                        body<List<KitchenOrder>> { description = "Active kitchen orders" }
                    }
                }
            }) {
                try {
                    val orders = kitchenRepository.getActiveKitchenOrders()
                    call.respond(HttpStatusCode.OK, orders)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Failed to retrieve kitchen orders"))
                    )
                }
            }

            put("/orders/{id}/status", {
                tags = listOf("Kitchen")
                summary = "Update kitchen order status"
                description = "Update the status of a kitchen order"
                request {
                    pathParameter<Int>("id") {
                        description = "Kitchen order ID"
                    }
                    body<KitchenOrderStatusUpdate> {
                        description = "New status details"
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = "Order status updated successfully"
                        body<KitchenOrder> { description = "Updated kitchen order" }
                    }
                    HttpStatusCode.NotFound to {
                        description = "Kitchen order not found"
                    }
                }
            }) {
                try {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid order ID")

                    val statusUpdate = call.receive<KitchenOrderStatusUpdate>()
                    val updatedOrder = kitchenRepository.updateKitchenOrderStatus(id, statusUpdate)

                    if (updatedOrder != null) {
                        call.respond(HttpStatusCode.OK, updatedOrder)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Kitchen order not found")
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Failed to update kitchen order status"))
                    )
                }
            }
        }
    }
} 