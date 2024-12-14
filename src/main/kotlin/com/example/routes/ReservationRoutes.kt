package com.example.routes

import com.example.models.*
import com.example.repositories.ReservationRepository
import io.github.smiley4.ktorswaggerui.dsl.routing.get
import io.github.smiley4.ktorswaggerui.dsl.routing.post
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class ReservationRoutes(private val reservationRepository: ReservationRepository) {

    fun Route.reservationRoutes() {
        route("/reservations") {
            post("/create", {
                tags = listOf("Reservation")
                summary = "Create a new reservation"
                description = "Create a new table reservation"
                request {
                    body<ReservationRequest> {
                        description = "Reservation details"
                    }
                }
                response {
                    HttpStatusCode.Created to {
                        description = "Reservation created successfully"
                        body<Reservation> { description = "Created reservation details" }
                    }
                    HttpStatusCode.BadRequest to {
                        description = "Invalid reservation data"
                    }
                }
            }) {
                try {
                    val request = call.receive<ReservationRequest>()
                    val reservation = Reservation(
                        customerName = request.customerName,
                        customerEmail = request.customerEmail,
                        customerPhone = request.customerPhone,
                        numberOfGuests = request.numberOfGuests,
                        reservationDate = request.reservationDate,
                        reservationTime = request.reservationTime,
                        status = ReservationStatus.PENDING,
                        specialRequests = request.specialRequests
                    )
                    val createdReservation = reservationRepository.createReservation(reservation)
                    call.respond(HttpStatusCode.Created, createdReservation)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to (e.message ?: "Failed to create reservation"))
                    )
                }
            }

            get("/all", {
                tags = listOf("Reservation")
                summary = "Get all reservations"
                description = "Retrieve a list of all reservations"
                response {
                    HttpStatusCode.OK to {
                        description = "List of all reservations"
                        body<List<Reservation>> { description = "List of reservations" }
                    }
                }
            }) {
                try {
                    val reservations = reservationRepository.getAllReservations()
                    call.respond(HttpStatusCode.OK, reservations)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Failed to retrieve reservations"))
                    )
                }
            }
        }
    }
} 