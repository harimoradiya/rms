package com.example.routes

import com.example.models.*
import com.example.repositories.TableRepository
import io.github.smiley4.ktorswaggerui.dsl.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class TableRoutes(private val tableRepository: TableRepository) {

    fun Route.tableRoutes() {
        route("/tables") {
            get("/available", {
                tags = listOf("Tables")
                summary = "Get available tables"
                description = "Retrieve all available tables"
                response {
                    HttpStatusCode.OK to {
                        description = "List of available tables"
                        body<List<Table>> { description = "Available tables" }
                    }
                }
            }) {
                try {
                    val tables = tableRepository.getAvailableTables()
                    call.respond(HttpStatusCode.OK, tables)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Failed to retrieve available tables"))
                    )
                }
            }

            put("/{id}/status", {
                tags = listOf("Tables")
                summary = "Update table status"
                description = "Update the status of a table (AVAILABLE, OCCUPIED, RESERVED)"
                request {
                    pathParameter<Int>("id") {
                        description = "Table ID"
                    }
                    body<StatusUpdateRequest> {
                        description = "New table status"
                        example("Update Status") {
                            value = mapOf(
                                "status" to "OCCUPIED"
                            )
                        }
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = "Table status updated successfully"
                        body<Table> { description = "Updated table details" }
                    }
                    HttpStatusCode.NotFound to {
                        description = "Table not found"
                    }
                }
            }) {
                try {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid table ID")
                    
                    val statusUpdate = call.receive<StatusUpdateRequest>()
                    val status = try {
                        TableStatus.valueOf(statusUpdate.status.uppercase())
                    } catch (e: IllegalArgumentException) {
                        return@put call.respond(HttpStatusCode.BadRequest, "Invalid status")
                    }

                    val updatedTable = tableRepository.updateTableStatus(id, status)
                    if (updatedTable != null) {
                        call.respond(HttpStatusCode.OK, updatedTable)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Table not found")
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Failed to update table status"))
                    )
                }
            }
        }
    }
} 