package com.example.routes

import com.example.models.MenuItem
import com.example.repositories.MenuRepository
import io.github.smiley4.ktorswaggerui.dsl.routing.route
import io.github.smiley4.ktorswaggerui.dsl.routing.get
import io.github.smiley4.ktorswaggerui.dsl.routing.post
import io.github.smiley4.ktorswaggerui.dsl.routing.put
import io.github.smiley4.ktorswaggerui.dsl.routing.delete
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.response.respond
import kotlin.reflect.typeOf


class MenuRoutes(private val menuRepository: MenuRepository) {
    fun Route.menuRoutes() {
        route("/v1/api/menu") {


            //Getting list of all menu
            get({
                tags = listOf("Menu")
                summary = "Retrieve all menu items"
                description = "Retrieve all menu items"
                response {
                    HttpStatusCode.OK to {
                        description = "Successfully retrieved menu items"
                        body<List<MenuItem>>() // Properly specifies the response body type
                    }
                    HttpStatusCode.InternalServerError to {
                        description = "Failed to retrieve menu items"
                    }
                }
            })
            {
                try {
                    val menuItems = menuRepository.getAll()
                    call.respond(HttpStatusCode.OK, menuItems)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        "Failed to retrieve menu items: ${e.message}"
                    )
                }
            }

            // POST create menu item
            post({
                tags = listOf("Menu")
                summary = "Create a new menu item"
                description = "Create a new menu item"
                request {
                    body<MenuItem> {description = "Menu item to be created"}
                }

                response {
                    HttpStatusCode.Created to {
                        description = "Menu item successfully created"
                        body<MenuItem> { description = "Created menu item"}
                    }
                    HttpStatusCode.BadRequest to {
                        description = "Invalid menu item data"
                    }
                }
            }
            ){

                try {
                    //Validate input
                    val menuItem = call.receive<MenuItem>()
                    //Validate required fields

                    val validationErrors = mutableListOf<String>()
                    if (menuItem.name.isBlank()) validationErrors.add("Name is required")
                    if (menuItem.price.isBlank()) validationErrors.add("Price is required")
                    if (menuItem.category.isBlank()) validationErrors.add("Category is required")


                    if (validationErrors.isNotEmpty()) {
                        return@post call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to validationErrors)
                        )
                    }

                    // Create menu item
                    val createdMenuItem = menuRepository.create(menuItem)
                    call.respond(HttpStatusCode.Created, createdMenuItem)
                }
                catch (e: Exception){
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        "Failed to create menu item : ${e.message}"
                    )

                }

            }



            // GET Menu Item by ID with Detailed Swagger Documentation
            get("/id/{id}", {
                tags = listOf("Menu")
                summary = "Get menu item by ID"
                description = "Retrieve a specific menu item using its unique identifier"

                request {
                    pathParameter("id", typeOf<Int>()) {
                        description = "Unique identifier of the menu item"
                        required = true
                    }
                }
                response {
                        HttpStatusCode.OK to {
                            description = "Successfully retrieved menu item"
                            body<MenuItem> { description = "Detailed menu item information" }
                        }
                        HttpStatusCode.NotFound to {
                            description = "Menu item with specified ID not found"
                        }
                        HttpStatusCode.BadRequest to {
                            description = "Invalid ID format"
                        }

                }
            }) {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid ID")

                try {
                    val menuItem = menuRepository.getById(id)
                    if (menuItem != null) {
                        call.respond(HttpStatusCode.OK, menuItem)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Menu item not found")
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Failed to retrieve menu item: ${e.message}")
                    )
                }
            }

            get("/isAvailable/{isAvailable}", {
                tags = listOf("Menu")
                summary = "Get all available menu items"
                description = "Retrieve a list of all available menu items"

                request {
                    pathParameter("isAvailable", typeOf<Boolean>()) {
                        description = "Enter true or false for getting available item"
                        required = true
                    }
                }

                response {
                    HttpStatusCode.OK to {
                        description = "Successfully retrieved available menu items"
                        body<List<MenuItem>> { description = "List of available menu items" }
                    }
                    HttpStatusCode.InternalServerError to {
                        description = "Failed to retrieve available menu items"
                    }
                }
            }){

                val isAvailable = call.parameters["isAvailable"]?.toBoolean()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid isAvailable parameter. Use 'true' or 'false'.")



                try {
                    val availableMenuItems = menuRepository.getAvailableItem(isAvailable)
                    call.respond(HttpStatusCode.OK, availableMenuItems)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        "Failed to retrieve available menu items: ${e.message}"
                    )
                }

            }


            put("/id/{id}", {
                tags = listOf("Menu")
                summary = "Update menu item by ID"
                description = "Update the details of a specific menu item using its unique identifier"

                request {
                    pathParameter("id", typeOf<Int>()) {
                        description = "Unique identifier of the menu item to be updated"
                        required = true
                    }
                    body<MenuItem> {
                        description = "Updated details of the menu item"
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = "Menu item successfully updated"
                        body<MenuItem> { description = "Updated menu item details" }
                    }
                    HttpStatusCode.NotFound to {
                        description = "Menu item with specified ID not found"
                    }
                    HttpStatusCode.BadRequest to {
                        description = "Invalid request data"
                    }
                    HttpStatusCode.InternalServerError to {
                        description = "Failed to update menu item"
                    }
                }
            }){

                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid ID")

                val updatedMenuItem = try {
                    call.receive<MenuItem>()
                } catch (e: Exception) {
                    return@put call.respond(HttpStatusCode.BadRequest, "Invalid menu item data")
                }
                try {
                    val existingMenuItem = menuRepository.getById(id)
                    if (existingMenuItem == null) {
                        call.respond(HttpStatusCode.NotFound, "Menu item not found")
                    } else {
                        val updatedItem = menuRepository.update(id, updatedMenuItem)
                        call.respond(HttpStatusCode.OK, updatedItem!!)
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to update menu item: ${e.message}")
                }


            }

            delete("/id/{id}",{
                tags = listOf("Menu")
                summary = "Delete menu item by ID"
                description = "Delete a specific menu item using its unique identifier"

                request {
                    pathParameter("id", typeOf<Int>()) {
                        description = "Unique identifier of the menu item to be deleted"
                        required = true
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = "Menu item successfully deleted"
                    }
                    HttpStatusCode.NotFound to {
                        description = "Menu item with specified ID not found"
                    }
                    HttpStatusCode.BadRequest to {
                        description = "Invalid ID"
                    }
                    HttpStatusCode.InternalServerError to {
                        description = "Failed to delete menu item"
                    }
                }
            }){

                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid ID")

                try {
                    val isDeleted = menuRepository.delete(id)
                    if (isDeleted) {
                        call.respond(HttpStatusCode.OK, "Menu item successfully deleted")
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Menu item not found")
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to delete menu item: ${e.message}")
                }

            }

        }

    }
}