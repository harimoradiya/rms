package com.example.routes

import com.example.models.*
import com.example.repositories.InventoryRepository
import io.github.smiley4.ktorswaggerui.dsl.routing.post
import io.github.smiley4.ktorswaggerui.dsl.routing.get
import io.github.smiley4.ktorswaggerui.dsl.routing.put
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class InventoryRoutes(private val inventoryRepository: InventoryRepository) {

    fun Route.inventoryRoutes() {
        route("/inventory") {
            post("/items", {
                tags = listOf("Inventory")
                summary = "Create new inventory item"
                description = "Add a new item to inventory"
                request {
                    body<InventoryItemRequest> {
                        description = "Inventory item details"
                    }
                }
                response {
                    HttpStatusCode.Created to {
                        description = "Item created successfully"
                        body<InventoryItem> { description = "Created inventory item" }
                    }
                    HttpStatusCode.BadRequest to {
                        description = "Invalid inventory item data"
                    }
                }
            }) {
                try {
                    val request = call.receive<InventoryItemRequest>()
                    
                    // Validate input
                    if (request.minimumStockLevel >= request.maximumStockLevel) {
                        call.respond(HttpStatusCode.BadRequest, "Minimum stock level must be less than maximum stock level")
                        return@post
                    }

                    val item = InventoryItem(
                        name = request.name,
                        description = request.description,
                        quantity = request.quantity,
                        unitType = request.unitType,
                        minimumStockLevel = request.minimumStockLevel,
                        maximumStockLevel = request.maximumStockLevel,
                        status = StockStatus.IN_STOCK, // Will be calculated in repository
                        cost = request.cost,
                        supplier = request.supplier,
                        location = request.location
                    )
                    
                    val createdItem = inventoryRepository.createInventoryItem(item)
                    call.respond(HttpStatusCode.Created, createdItem)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to (e.message ?: "Failed to create inventory item"))
                    )
                }
            }

            put("/items/{id}/stock", {
                tags = listOf("Inventory")
                summary = "Update stock quantity"
                description = "Update the stock quantity of an inventory item"
                request {
                    pathParameter<Int>("id") {
                        description = "Inventory item ID"
                    }
                    body<StockUpdateRequest> {
                        description = "Stock update details"
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = "Stock updated successfully"
                        body<InventoryItem> { description = "Updated inventory item" }
                    }
                    HttpStatusCode.NotFound to {
                        description = "Inventory item not found"
                    }
                }
            }) {
                try {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid item ID")

                    val update = call.receive<StockUpdateRequest>()
                    
                    // Get user ID from JWT token if available
                    val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()
                    
                    val updatedItem = inventoryRepository.updateStock(id, update, userId)
                    call.respond(HttpStatusCode.OK, updatedItem)
                } catch (e: Exception) {
                    when (e.message) {
                        "Inventory item not found" -> call.respond(HttpStatusCode.NotFound, "Inventory item not found")
                        else -> call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to (e.message ?: "Failed to update stock"))
                        )
                    }
                }
            }

            get("/items/low-stock", {
                tags = listOf("Inventory")
                summary = "Get low stock items"
                description = "Retrieve all items with low stock status"
                response {
                    HttpStatusCode.OK to {
                        description = "List of low stock items"
                        body<List<InventoryItem>> { description = "Low stock items" }
                    }
                }
            }) {
                try {
                    val items = inventoryRepository.getLowStockItems()
                    call.respond(HttpStatusCode.OK, items)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Failed to retrieve low stock items"))
                    )
                }
            }

            get("/items/{id}/transactions", {
                tags = listOf("Inventory")
                summary = "Get item transactions"
                description = "Retrieve transaction history for an inventory item"
                request {
                    pathParameter<Int>("id") {
                        description = "Inventory item ID"
                    }
                    queryParameter<Int>("limit") {
                        description = "Number of transactions to retrieve"
                        required = false
//                        defaultValue = 10
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = "Transaction history retrieved successfully"
                        body<List<InventoryTransaction>> { description = "List of transactions" }
                    }
                    HttpStatusCode.NotFound to {
                        description = "Inventory item not found"
                    }
                }
            }) {
                try {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid item ID")
                    
                    val limit = call.parameters["limit"]?.toIntOrNull() ?: 10
                    
                    // Verify item exists
                    if (inventoryRepository.getInventoryItemById(id) == null) {
                        call.respond(HttpStatusCode.NotFound, "Inventory item not found")
                        return@get
                    }

                    val transactions = inventoryRepository.getItemTransactions(id, limit)
                    call.respond(HttpStatusCode.OK, transactions)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Failed to retrieve transactions"))
                    )
                }
            }
        }
    }
} 