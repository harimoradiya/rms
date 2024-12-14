package com.example.routes

import com.example.database.FeedbackTable.userId
import com.example.models.*
import com.example.repositories.OrderRepository
import io.github.smiley4.ktorswaggerui.dsl.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class OrderRoutes(private val orderRepository: OrderRepository) {

    fun Route.orderRoutes() {
        route("/orders") {

            post("/create", {
                summary = "Create a new order"
                description = "Create a new order with items"
                tags = listOf("Orders")

                request {
                    body<CreateOrderRequest> {
                        description = "Order details"
                        example("Sample Order") {
                            value = mapOf(
                                "tableId" to 1,
                                "items" to listOf(
                                    mapOf(
                                        "menuItemId" to 1,
                                        "quantity" to 2,
                                        "specialInstructions" to "Extra spicy"
                                    )
                                )
                            )
                        }
                    }
                }
                response {
                    HttpStatusCode.Created to {
                        description = "Order created successfully"
                        body<Order> { description = "Created order details" }
                    }
                    HttpStatusCode.Unauthorized to {
                        description = "Invalid or missing token"
                    }
                    HttpStatusCode.BadRequest to {
                        description = "Invalid order data"
                    }
                }
            })
            {
                try {
                    val request = call.receive<CreateOrderRequest>()
                    val createdOrder = orderRepository.createOrder(request)
                    call.respond(HttpStatusCode.Created, createdOrder)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to (e.message ?: "Failed to create order"))
                    )
                }
            }


                get("/{id}", {
                    summary = "Get order by ID"
                    description = "Retrieve a specific order by its ID"
                    tags = listOf("Orders")
                    request {
                        pathParameter<Int>("id") {
                            description = "Order ID"
                            example("1"){

                            }
                        }
                    }
                    response {
                        HttpStatusCode.OK to {
                            description = "Order found"
                            body<Order> { description = "Order details" }
                        }
                        HttpStatusCode.NotFound to {
                            description = "Order not found"
                        }
                    }
                }) {
                    try {
                        val id = call.parameters["id"]?.toIntOrNull()
                            ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid order ID")

                        val order = orderRepository.getOrderById(id)
                        if (order != null) {
                            call.respond(HttpStatusCode.OK, order)
                        } else {
                            call.respond(HttpStatusCode.NotFound, "Order not found")
                        }
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to (e.message ?: "Failed to retrieve order"))
                        )
                    }
                }
            }

        get("/all", {
                    summary = "Get all orders"
                    description = "Retrieve a list of all orders with their items"
                    tags = listOf("Orders")
                    response {
                        HttpStatusCode.OK to {
                            description = "List of all orders"
                            body<List<Order>> { 
                                description = "List of orders"
                                example("Order List") {
                                    value = listOf(
                                        mapOf(
                                            "id" to 1,
                                            "tableId" to 1,
                                            "status" to "PENDING",
                                            "totalAmount" to 50.0,
                                            "items" to listOf(
                                                mapOf(
                                                    "id" to 1,
                                                    "orderId" to 1,
                                                    "menuItemId" to 1,
                                                    "quantity" to 2,
                                                    "itemPrice" to 25.0
                                                )
                                            )
                                        )
                                    )
                                }
                            }
                        }
                    }
                }) {
                    try {
                        val orders = orderRepository.getAllOrders()
                        call.respond(HttpStatusCode.OK, orders)
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to (e.message ?: "Failed to retrieve orders"))
                        )
                    }
                }

                put("/{id}/status", {
                    summary = "Update order status"
                    description = "Update the status of an existing order"
                    tags = listOf("Orders")
                    request {
                        pathParameter<Int>("id") {
                            description = "Order ID"
                            example("1"){
                                "1"
                            }
                        }
                        body<OrderStatus> {
                            description = "New order status"
                            example("OrderStatus"){
                                OrderStatus.PREPARING
                            }
                        }
                    }
                    response {
                        HttpStatusCode.OK to {
                            description = "Order status updated successfully"
                        }
                        HttpStatusCode.NotFound to {
                            description = "Order not found"
                        }
                    }
                }) {
                    try {
                        val id = call.parameters["id"]?.toIntOrNull()
                            ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid order ID")
                        
                        val newStatus = call.receive<OrderStatus>()
                        val updated = orderRepository.updateOrderStatus(id, newStatus)
                        
                        if (updated) {
                            call.respond(HttpStatusCode.OK, "Order status updated successfully")
                        } else {
                            call.respond(HttpStatusCode.NotFound, "Order not found")
                        }
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to (e.message ?: "Failed to update order status"))
                        )
                    }
                }

                delete("/{id}", {
                    summary = "Delete order"
                    description = "Delete an order and all its items"
                    tags = listOf("Orders")
                    request {
                        pathParameter<Int>("id") {
                            description = "Order ID"
//                            example = 1
                        }
                    }
                    response {
                        HttpStatusCode.OK to {
                            description = "Order deleted successfully"
                        }
                        HttpStatusCode.NotFound to {
                            description = "Order not found"
                        }
                    }
                }) {
                    try {
                        val id = call.parameters["id"]?.toIntOrNull()
                            ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid order ID")

                        val deleted = orderRepository.deleteOrder(id)
                        if (deleted) {
                            call.respond(HttpStatusCode.OK, "Order deleted successfully")
                        } else {
                            call.respond(HttpStatusCode.NotFound, "Order not found")
                        }
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to (e.message ?: "Failed to delete order"))
                        )
                    }
                }
            }
}

